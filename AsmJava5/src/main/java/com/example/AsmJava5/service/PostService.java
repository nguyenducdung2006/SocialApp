package com.example.AsmJava5.service;

import com.example.AsmJava5.model.Post;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post upload(User user, String title, String description,
                       String tags, MultipartFile image) throws Exception {
        Post post = Post.builder()
                .user(user)
                .title(title)
                .description(description)
                .tags(tags)
                .isDeleted(false)
                .views(0)
                .likesCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (image != null && !image.isEmpty()) {
            post.setImageData(image.getBytes());
            post.setImageName(image.getOriginalFilename());
        }

        return postRepository.save(post);
    }

    // ✅ Không truyền Pageable
    public List<Post> getAllActivePosts() {
        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
    }

    // ✅ Đúng method name
    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    public byte[] getImageData(Long postId) {
        return postRepository.findById(postId)
                .map(Post::getImageData)
                .orElse(null);
    }

    public void softDelete(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        if (!post.getUser().getId().equals(userId))
            throw new RuntimeException("Không có quyền xóa!");

        post.setIsDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    public Post update(Long postId, Long userId, String title,
                       String description, String tags,
                       MultipartFile image) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng!"));

        if (!post.getUser().getId().equals(userId))
            throw new RuntimeException("Không có quyền sửa!");

        post.setTitle(title);
        post.setDescription(description);
        post.setTags(tags);
        post.setUpdatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            post.setImageData(image.getBytes());
            post.setImageName(image.getOriginalFilename());
        }

        return postRepository.save(post);
    }
}
