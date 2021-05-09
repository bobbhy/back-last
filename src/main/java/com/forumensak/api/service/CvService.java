package com.forumensak.api.service;

import com.forumensak.api.exception.AppException;
import com.forumensak.api.model.Role;
import com.forumensak.api.model.User;
import com.forumensak.api.model.cv.*;
import com.forumensak.api.model.social.Comment;
import com.forumensak.api.model.social.Like;
import com.forumensak.api.model.social.Notification;
import com.forumensak.api.model.social.Post;
import com.forumensak.api.payload.NotificationEmail;
import com.forumensak.api.payload.ResponseMessage;
import com.forumensak.api.repository.*;
import com.forumensak.api.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CvService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    FilesStorageService storageService;
    @Autowired
    ExperienceRepository experienceRepository;
    @Autowired
    EducationRepository educationRepository;
    @Autowired
    AwardRepository awardRepository;
    @Autowired
    AboutRepository aboutRepository;
    @Autowired
    DevLanguageRepository devLanguageRepository;
    @Autowired
    NormalLanguageRepository normalLanguageRepository;
    @Autowired
    SoftwareRepository softwareRepository;
    @Autowired
    CvRepository cvRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    MailService mailService;
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    LinkRepository linkRepository;

    public static String encoder(String imagePath) {
        String base64Image = "";
        File file = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a Image file from file system
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            base64Image = Base64.getEncoder().encodeToString(imageData);
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return base64Image;
    }

    public ResponseEntity<?> uploadImage(MultipartFile file, String authHeader) {
        String message = "";
        try {
            storageService.save(file);
            message = "Uploaded the file successfully!";
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
            user.getCv().setImage(file.getOriginalFilename());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getImage(String authHeader) {
        String message = "";
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
            Resource resource = storageService.load(user.getCv().getImage());
            return ResponseEntity.status(HttpStatus.OK).body(encoder(resource.getURI().getPath()));
        } catch (Exception e) {
            message = "Could not upload the file: !";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getImageById(long id) {
        String message = "";
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
            Resource resource = storageService.load(user.getCv().getImage());
            return ResponseEntity.status(HttpStatus.OK).body(encoder(resource.getURI().getPath()));
        } catch (Exception e) {
            message = "Could not upload the file: !";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }


    public ResponseEntity<?> uploadAbout(About about, String authHeader) {
        String message = "";
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));

            user.getCv().getAbout().setNumber(about.getNumber());
            user.getCv().getAbout().setCity(about.getCity());
            if (!about.getFirstName().isEmpty() && !about.getLastName().isEmpty()) {
                user.getCv().getAbout().setFirstName(about.getFirstName());
                user.getCv().getAbout().setLastName(about.getLastName());
            }
            user.getCv().getAbout().setBio(about.getBio());
            user.getCv().getAbout().setAddress(about.getAddress());
            user.getCv().getAbout().setInterests(about.getInterests());
            user.getCv().getAbout().setDomaine(about.getDomaine());
            userRepository.save(user);
            message = "Uploaded!";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> uploadExperience(Experience experience, String authHeader) {
        String message = "";
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            experience.setCv(user.getCv());
            experienceRepository.save(experience);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(experience.getId()).toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> uploadLink(Link link, String authHeader) {
        String message = "";
        boolean k = true;
        try {
            String jwt = getJwtFromHeader(authHeader);
            long linkId = 0;
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            List<Link> linkList = user.getCv().getLinks();
            for (Link e : linkList) {
                if (e.getName().equals(link.getName())) {
                    linkId = e.getId();
                    k = false;
                    break;
                }
            }
            if (k) {
                link.setCv(user.getCv());
                linkRepository.save(link);
                userRepository.save(user);
            } else {
                Link putLink = linkRepository.findById(linkId)
                        .orElseThrow(() -> new AppException("User id doesn't exist"));
                putLink.setUrl(link.getUrl());
                linkRepository.save(putLink);
                userRepository.save(user);
            }
            return ResponseEntity.status(HttpStatus.OK).body(linkId);
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getLinks(String authHeader) {
        String message = "";
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        Optional<User> userOptional = Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
        User user = userOptional.get();
        return ResponseEntity.status(HttpStatus.OK).body(user.getCv().getLinks());
    }

    public ResponseEntity<?> getExperience(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        List<Experience> experienceList = user.getCv().getExperiences();
        return ResponseEntity.status(HttpStatus.OK).body(experienceList);
    }

    public ResponseEntity<?> getExperienceById(long id) {
        String message = "";
        try {
            Experience experience = experienceRepository.findById(id)
                    .orElseThrow(() -> new AppException("User id doesn't exist"));
            return ResponseEntity.status(HttpStatus.OK).body(experience);
        } catch (Exception e) {
            message = "Could not get experience with id " + id;
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> deleteExperience(long id) {
        experienceRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> uploadEducation(Education education, String authHeader) {
        String message = education.toString();
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            education.setCv(user.getCv());
            educationRepository.save(education);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(education.getId()).toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getEducation(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        // List<Experience> experienceList =
        // experienceRepository.findAllByCv(user.getCv());
        List<Education> educationList = user.getCv().getEducations();
        return ResponseEntity.status(HttpStatus.OK).body(educationList);
    }

    public ResponseEntity<?> deleteEducation(long id) {
        educationRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> uploadAward(Award award, String authHeader) {
        String message = award.toString();
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            award.setCv(user.getCv());
            awardRepository.save(award);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(award.getId())
                    .toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getAward(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        // List<Experience> experienceList =
        // experienceRepository.findAllByCv(user.getCv());
        List<Award> awardList = user.getCv().getAwards();
        return ResponseEntity.status(HttpStatus.OK).body(awardList);
    }

    public ResponseEntity<?> deleteAward(long id) {
        awardRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> getCv(String authHeader) {
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            return ResponseEntity.ok(user.getCv());
        } catch (Exception e) {
            String message = "Error!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getCvById(long id) {
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            String message = "Error!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> uploadDevLanguage(DevLanguage devLanguage, String authHeader) {
        String message = devLanguage.toString();
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            devLanguage.setCv(user.getCv());
            devLanguageRepository.save(devLanguage);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(devLanguage.getId()).toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getDevLanguage(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        List<DevLanguage> devLanguageList = user.getCv().getDevLanguages();
        return ResponseEntity.status(HttpStatus.OK).body(devLanguageList);
    }

    public ResponseEntity<?> deleteDevLanguage(long id) {
        devLanguageRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> uploadNormalLanguage(NormalLanguage normalLanguage, String authHeader) {
        String message = normalLanguage.toString();
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            normalLanguage.setCv(user.getCv());
            normalLanguageRepository.save(normalLanguage);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(normalLanguage.getId()).toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getNormalLanguage(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        List<NormalLanguage> normalLanguageList = user.getCv().getNormalLanguages();
        return ResponseEntity.status(HttpStatus.OK).body(normalLanguageList);
    }

    public ResponseEntity<?> deleteNormalLanguage(long id) {
        normalLanguageRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> uploadSoftware(Software software, String authHeader) {
        String message = software.toString();
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            software.setCv(user.getCv());
            softwareRepository.save(software);
            userRepository.save(user);
            // getting the path of the post and append id of the post to the URI
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(software.getId()).toUri();
            // returns the location of the created post
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getSoftware(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
        List<Software> softwareList = user.getCv().getSoftwares();
        return ResponseEntity.status(HttpStatus.OK).body(softwareList);
    }

    public ResponseEntity<?> deleteSoftware(long id) {
        softwareRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Error");
    }

    public List<About> listAll(String keyword) {
        if (keyword != null) {
            return aboutRepository.search(keyword);
        }
        return aboutRepository.findAll();
    }

    public ResponseEntity<?> getAbbout(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        Optional<User> userOptional = Optional.ofNullable(
                userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
        User user = userOptional.get();
        return ResponseEntity.status(HttpStatus.OK).body(user.getCv().getAbout());

    }

    public ResponseEntity<?> updateAbout(long id, About aboutDetails) {
        String message = "";
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            About about = user.getCv().getAbout();
            about.setFirstName(aboutDetails.getFirstName());
            about.setLastName(aboutDetails.getLastName());
            about.setAddress(aboutDetails.getAddress());
            about.setCity(aboutDetails.getCity());
            about.setInterests(aboutDetails.getInterests());
            about.setBio(aboutDetails.getBio());
            about.setNumber(aboutDetails.getNumber());
            about.setDomaine(aboutDetails.getDomaine());
            About updatedAbout = aboutRepository.save(about);
            return ResponseEntity.status(HttpStatus.OK).body(updatedAbout);
        } catch (Exception e) {
            message = "Could not edit the about section due to a server error!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> updateExperience(long id, Experience experienceDetails) {
        String message = "";
        try {
            Experience experience = experienceRepository.findById(id)
                    .orElseThrow(() -> new AppException("User id doesn't exist"));
            experience.setOccupation(experienceDetails.getOccupation());
            experience.setCompany(experienceDetails.getCompany());
            experience.setDateStart(experienceDetails.getDateStart());
            experience.setDateEnd(experienceDetails.getDateEnd());
            experience.setDescription(experienceDetails.getDescription());
            Experience updatedExperience = experienceRepository.save(experience);
            return ResponseEntity.status(HttpStatus.OK).body(updatedExperience);
        } catch (Exception e) {
            message = "Could not edit experiences!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> updateEducation(long id, Education educationDetails) {
        String message = "";
        try {
            Education education = educationRepository.findById(id)
                    .orElseThrow(() -> new AppException("User id doesn't exist"));
            education.setSchool(educationDetails.getSchool());
            education.setDiploma(educationDetails.getDiploma());
            education.setDateStart(educationDetails.getDateStart());
            education.setDateEnd(educationDetails.getDateEnd());
            education.setField(educationDetails.getField());
            Education updatedEducation = educationRepository.save(education);
            return ResponseEntity.status(HttpStatus.OK).body(updatedEducation);
        } catch (Exception e) {
            message = "Could not edit education!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getEducationById(long id) {
        String message = "";
        try {
            Education education = educationRepository.findById(id)
                    .orElseThrow(() -> new AppException("User id doesn't exist"));
            return ResponseEntity.status(HttpStatus.OK).body(education);
        } catch (Exception e) {
            message = "Could not get experience with id " + id;
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> turnFlag(long id) {
        String message = "";
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            user.getCv().setFlag(true);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(user.getCv().isFlag());
        } catch (Exception e) {
            message = "Couldn't!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> uploadPost(Post post, String authHeader) {
        String message = "";
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            Post newPost = new Post();
            newPost.setOwner(user);
            newPost.setOwnerFirstName(user.getCv().getAbout().getFirstName());
            newPost.setOwnerLastName(user.getCv().getAbout().getLastName());
            newPost.setOwnerImage(user.getCv().getImage());
            newPost.setMessage(post.getMessage());
            newPost.setOwnersId(id);
            newPost.setDomaine(post.getDomaine());
            newPost.setImageType(post.getImageType());
            newPost.setOwnerUsername(user.getUsername());
            newPost.setRole(post.getRole());
            postRepository.save(newPost);
            userRepository.save(user);
            // returns the location of the created post
            return ResponseEntity.status(HttpStatus.OK).body(newPost.getId());
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getPosts() {
        List<Post> postList = postRepository.findAll();
        postList.sort((b, a) -> a.getUpdatedAt().compareTo(b.getUpdatedAt()));
        return ResponseEntity.status(HttpStatus.OK).body(postList);
    }

    public ResponseEntity<?> getNonPrivatePosts(String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        List<Post> postList = postRepository.findAll();
        postList.sort((b, a) -> a.getUpdatedAt().compareTo(b.getUpdatedAt()));
        postList = postList.stream().filter(post -> {
            Role userRole = roleRepository.findById(post.getRole())
                    .orElseThrow(() -> new AppException("User Role not set."));
            if (post.getOwnersId() == id) {
                return true;
            } else if (userRole.getId() == 1) {
                return !post.getOwner().getCv().isPrive();
            } else {
                return true;
            }
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(postList);
    }

    public ResponseEntity<?> getPostById(long postId) {
        Optional<Post> postOptional = Optional
                .ofNullable(postRepository.findById(postId).orElseThrow(() -> new AppException("Post doesn't exist")));
        Post post = postOptional.get();
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    public ResponseEntity<?> deletePost(long id) {
        postRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> updatePost(long id, Post postDetails) {
        String message = "";
        try {
            Post post = postRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist"));
            post.setMessage(postDetails.getMessage());
            Post updatedPost = postRepository.save(post);
            return ResponseEntity.status(HttpStatus.OK).body(updatedPost);
        } catch (Exception e) {
            message = "Could not edit post!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> uploadComment(long postId, Comment comment, String authHeader) {
        String message = "";
        try {
            String jwt = getJwtFromHeader(authHeader);
            long id = jwtTokenProvider.getUserIdFromJWT(jwt);
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            Comment newComment = new Comment();
            Optional<Post> postOptional = Optional.ofNullable(
                    postRepository.findById(postId).orElseThrow(() -> new AppException("Post doesn't exist")));
            Post post = postOptional.get();

            User postOwner = post.getOwner();

            newComment.setOwner(user);
            newComment.setPost(post);
            newComment.setOwnerFirstName(user.getCv().getAbout().getFirstName());
            newComment.setOwnerLastName(user.getCv().getAbout().getLastName());
            newComment.setOwnerImage(user.getCv().getImage());
            newComment.setMessage(comment.getMessage());
            newComment.setPostsId(postId);
            newComment.setOwnersId(id);
            newComment.setOwnerUsername(user.getUsername());
            newComment.setRole(comment.getRole());

            if (postOwner.getId() != user.getId()) {
                Notification notification = new Notification();
                notification.setMessage("has commented on your post");
                notification.setStatus(false);
                notification.setOwner(postOwner);
                notification.setOwnerName(
                        user.getCv().getAbout().getFirstName() + " " + user.getCv().getAbout().getLastName());
                notification.setOwnersId(user.getId());
                notification.setOwnerUsername(user.getUsername());
                notification.setPostId(postId);
                notification.setOwnerImage(user.getCv().getImage());
                notificationRepository.save(notification);
            }
            commentRepository.save(newComment);
            userRepository.save(user);
            // returns the location of the created post
            return ResponseEntity.status(HttpStatus.OK).body(newComment);
        } catch (Exception e) {
            message = "Could not upload!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> deleteComment(long id) {
        commentRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> updateComment(long id, Comment commentDetails) {
        String message = "";
        try {
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new AppException("Comment doesn't exist"));
            comment.setMessage(commentDetails.getMessage());
            Comment updatedComment = commentRepository.save(comment);
            return ResponseEntity.status(HttpStatus.OK).body(updatedComment);
        } catch (Exception e) {
            message = "Could not edit post!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> priveCv(long id) {
        String message = "";
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            user.getCv().setPrive(true);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            message = "Couldn't!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> unpriveCv(long id) {
        String message = "";
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            user.getCv().setPrive(false);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            message = "Couldn't!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> likeComment(long commentId, long id) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Comment doesn't exist"));
        List<Like> likeList = comment.getLikes();
        for (Like e : likeList) { // just in case
            if (e.getLikerId() == id) {
                return ResponseEntity.status(HttpStatus.OK).body(comment);
            }
        }
        Like like = new Like();
        like.setLikerId(id);
        like.setComment(comment);
        User postOwner = comment.getOwner();
        Optional<User> userOptional = Optional
                .ofNullable(userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
        User user = userOptional.get();
        long postId = comment.getPostsId();
        if (postOwner.getId() != user.getId()) {
            Notification notification = new Notification();
            notification.setMessage("has liked your comment");
            notification.setStatus(false);
            notification.setOwner(postOwner);
            if (user.getRoles().iterator().next().getId() == 1) {
                notification.setOwnerName(
                        user.getCv().getAbout().getFirstName() + " " + user.getCv().getAbout().getLastName());
                notification.setOwnersId(user.getId());
                notification.setOwnerUsername(user.getUsername());
                notification.setPostId(postId);
                notification.setOwnerImage(user.getCv().getImage());
            } else if (user.getRoles().iterator().next().getId() == 3) {
                notification.setOwnerName(user.getCompany().getAboutCompany().getName());
                notification.setPostId(postId);
                notification.setOwnersId(user.getId());
                notification.setOwnerUsername(user.getUsername());
                notification.setOwnerImage(user.getCompany().getCompanyImage());
            }
            notificationRepository.save(notification);
        }
        likeRepository.save(like);
        return ResponseEntity.status(HttpStatus.OK).body(comment);
    }

    public ResponseEntity<?> unlikeComment(long commentId, String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Comment doesn't exist"));
        List<Like> likeList = comment.getLikes();
        for (Like e : likeList) { // just in case
            if (e.getLikerId() == id) {
                likeRepository.deleteById(e.getId());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> unlikePost(long postId, String authHeader) {
        String jwt = getJwtFromHeader(authHeader);
        long id = jwtTokenProvider.getUserIdFromJWT(jwt);
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException("Comment doesn't exist"));
        List<Like> likeList = post.getLikes();
        for (Like e : likeList) { // just in case
            if (e.getLikerId() == id) {
                likeRepository.deleteById(e.getId());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfuly");
    }

    public ResponseEntity<?> likePost(long postId, long id) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException("Comment doesn't exist"));
        List<Like> likeList = post.getLikes();
        for (Like e : likeList) { // just in case
            if (e.getLikerId() == id) {
                return ResponseEntity.status(HttpStatus.OK).body(post);
            }
        }
        User postOwner = post.getOwner();
        Optional<User> userOptional = Optional
                .ofNullable(userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
        User user = userOptional.get();
        Like like = new Like();
        like.setLikerId(id);
        like.setPost(post);
        if (postOwner.getId() != user.getId()) {
            Notification notification = new Notification();
            notification.setMessage("has liked your post");
            notification.setStatus(false);
            notification.setOwner(postOwner);
            if (user.getRoles().iterator().next().getId() == 1) {
                notification.setOwnerName(
                        user.getCv().getAbout().getFirstName() + " " + user.getCv().getAbout().getLastName());
                notification.setOwnersId(user.getId());
                notification.setOwnerUsername(user.getUsername());
                notification.setPostId(postId);
                notification.setOwnerImage(user.getCv().getImage());
            } else if (user.getRoles().iterator().next().getId() == 3) {
                notification.setOwnerName(user.getCompany().getAboutCompany().getName());
                notification.setPostId(postId);
                notification.setOwnersId(user.getId());
                notification.setOwnerUsername(user.getUsername());
                notification.setOwnerImage(user.getCompany().getCompanyImage());
            }
            notificationRepository.save(notification);
        }
        likeRepository.save(like);
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    public ResponseEntity<?> getComments(long postId) {
        String message = "";
        try {
            Optional<Post> postOptional = Optional.ofNullable(
                    postRepository.findById(postId).orElseThrow(() -> new AppException("Post doesn't exist")));
            Post post = postOptional.get();
            List<Comment> commentList = post.getComment();
            return ResponseEntity.status(HttpStatus.OK).body(commentList);
        } catch (Exception e) {
            message = "Couldnt get comments!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getLikes() {
        List<Like> likeList = likeRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(likeList);
    }

    public ResponseEntity<?> getLikesByPost(long postId) {
        Optional<Post> postOptional = Optional
                .ofNullable(postRepository.findById(postId).orElseThrow(() -> new AppException("Post doesn't exist")));
        Post post = postOptional.get();
        List<Like> likeList = likeRepository.findAllByPost(post);
        return ResponseEntity.status(HttpStatus.OK).body(likeList);
    }

    public ResponseEntity<?> enableAccount(long id) {
        String message = "";
        try {
            Optional<User> userOptional = Optional.ofNullable(
                    userRepository.findById(id).orElseThrow(() -> new AppException("User id doesn't exist")));
            User user = userOptional.get();
            user.setEnabled(true);
            userRepository.save(user);
            mailService.sendEmail(new NotificationEmail("Your account has been enabled", user.getEmail(),
                    "Activate user," + user.getUsername() + ", " + user.getEmail() + "by clicking here :\n"
                            + "<a href=\"http://localhost:3000/login" + "\">Login</a>"));
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            message = "Couldn't!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    public ResponseEntity<?> getAllStudents() {
        try {
            List<User> users = userRepository.findAll();
            users = users.stream().filter(user -> user.getRoles().iterator().next().getId() == 1)
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception e) {
            String message = "Error!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    private String getJwtFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}