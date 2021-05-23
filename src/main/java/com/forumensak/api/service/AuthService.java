package com.forumensak.api.service;

import com.forumensak.api.exception.AppException;
import com.forumensak.api.exception.BadRequestException;
import com.forumensak.api.model.*;
import com.forumensak.api.model.company.AboutCompany;
import com.forumensak.api.model.cv.About;
import com.forumensak.api.model.social.Comment;
import com.forumensak.api.model.social.Post;
import com.forumensak.api.payload.*;
import com.forumensak.api.repository.*;
import com.forumensak.api.security.JwtTokenProvider;
import com.forumensak.api.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    EtablishementRepository etablishementRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenProvider tokenProvider;
    @Autowired
    VerificationTokenRepository verificationTokenRepository;
    @Autowired
    MailService mailService;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    @Value("${app.front}")
    private String front;

    public ResponseEntity<?> signIn(SignInRequest signInRequest) {
        if (!userRepository
                .findByUsernameOrEmail(signInRequest.getUsernameOrEmail(), signInRequest.getUsernameOrEmail()).get()
                .getEnabled()) {
            return new ResponseEntity(new ApiResponse(false, "Enable your account first!"), HttpStatus.UNAUTHORIZED);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    signInRequest.getUsernameOrEmail(), signInRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } catch (Exception e) {
            return new ResponseEntity(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public ResponseEntity<?> signUp(SignUpRequest signUpRequest, long roleId) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"), HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getCompanyName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findById(roleId).orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));
        user.setEnabled(false);
        user.setReported(false);
        if (userRole.getId() == 1) {
            if (!signUpRequest.getEmail().endsWith("@uit.ac.ma")) {
                return new ResponseEntity(new ApiResponse(false, "Use your university mail"), HttpStatus.BAD_REQUEST);
            }
            Etablishment etablishment = etablishementRepository.getById(signUpRequest.getEtablishment_id());
            user.setEtablishment(etablishment);
            user.setCv(new Cv());
            user.getCv().setFlag(false);
            user.getCv().setAbout(new About());
            user.getCv().setPrive(false);
        } else if (userRole.getId() == 3) {
            user.setCompany(new Company());
            user.getCompany().setFlag(false);
            user.getCompany().setAboutCompany(new AboutCompany());
        } else if (userRole.getId() == 2) {
            user.setEnabled(true);
        }
        User result = userRepository.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();
        // Generate token
        String token = generateVerificationToken(user);
        if (userRole.getId() == 1) {
            mailService.sendEmail(new NotificationEmail("Please activate your account", user.getEmail(),
                    "Thank you for signing up to forum ensak,"
                            + "please click on the below url to activate your account :\n" + "<br/><a href=\"" + front
                            + "/confirm?token=" + token + "\">" + front + "/confirm?token" + token + "</a>"));
        }
        if (userRole.getId() == 3) {
            mailService.sendEmail(new NotificationEmail("Activate account for enterprise manager",
                    "aymane.elmouhtarim@gmail.com", "Activate user," + user.getUsername() + ", " + user.getEmail()
                            + "by clicking here :\n" + "<a href=\"" + front + "/admin" + "\">Check it out</a>"));
        }

        return ResponseEntity.created(location).body(new ApiResponse(true,
                "User registered successfully, Please enable your account through your mail box"));
    }

    public ResponseEntity<?> verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(() -> new AppException("Invalid Token"));
        if (!verificationToken.get().isExpired()) {
            User user = fetchUserAndEnable(verificationToken.get());
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toList());
            Authentication authentication = new UsernamePasswordAuthenticationToken(new UserPrincipal(user.getId(),
                    user.getName(), user.getUsername(), user.getEmail(), user.getPassword(), authorities), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } else {
            return ResponseEntity.ok("Token is expired");
        }
    }

    public String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpired(false);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public User fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User no longer exist in database"));
        user.setEnabled(true);
        verificationToken.setExpired(true);
        userRepository.save(user);
        return user;

    }

    public ResponseEntity<?> deleteUser(Long id) {
        VerificationToken verificationToken = verificationTokenRepository.findByUserId(id)
                .orElseThrow(() -> new AppException("token not found"));
        verificationToken.getUser().setEtablishment(null);
        verificationTokenRepository.delete(verificationToken);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> disableAccount(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User no longer exist in database"));
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> enableAccount(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User no longer exist in database"));
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> reportAccountById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
        user.setReported(true);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> unreportAccountById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
        user.setReported(false);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> reportAccountByPost(long postId) {
        Optional<Post> postOptional = Optional
                .ofNullable(postRepository.findById(postId).orElseThrow(() -> new AppException("Post doesn't exist")));
        Post post = postOptional.get();
        User user = userRepository.findById(post.getOwnersId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setReported(true);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    public ResponseEntity<?> reportAccountByComment(long commentId) {
        Optional<Comment> commentOptional = Optional.ofNullable(
                commentRepository.findById(commentId).orElseThrow(() -> new AppException("Comment doesn't exist")));
        Comment comment = commentOptional.get();
        User user = userRepository.findById(comment.getOwnersId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setReported(true);
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

}
