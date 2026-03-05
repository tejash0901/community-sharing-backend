package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.dto.AuthResponse;
import com.community.toolsharing.backend.dto.LoginRequest;
import com.community.toolsharing.backend.dto.RegisterRequest;
import com.community.toolsharing.backend.exception.BadRequestException;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.Community;
import com.community.toolsharing.backend.repository.CommunityRepository;
import com.community.toolsharing.backend.repository.UserRepository;
import com.community.toolsharing.backend.security.AppUserDetails;
import com.community.toolsharing.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       CommunityRepository communityRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        Community community = communityRepository.findByInviteCode(request.getCommunityInviteCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid community invite code"));

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCommunity(community);
        user.setBlock(request.getBlock());
        user.setFloor(request.getFloor());
        user.setFlatNumber(request.getFlatNumber());

        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(new AppUserDetails(saved));
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), community.getId(), community.getName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(new AppUserDetails(user));
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCommunity().getId(),
                user.getCommunity().getName()
        );
    }
}