package kr.ac.kopo.su.school_meal_login.controller;

import kr.ac.kopo.su.school_meal_login.dto.*;
import kr.ac.kopo.su.school_meal_login.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public String postList(@RequestParam(required = false) String date,
                           @RequestParam(required = false) String mealType,
                           Model model) {
        try {
            List<PostDto> posts;

            if (date != null && mealType != null) {
                posts = postService.getPostsByMeal(date, mealType);
                model.addAttribute("selectedDate", date);
                model.addAttribute("selectedMealType", mealType);
            } else {
                posts = postService.getAllPosts();
            }

            model.addAttribute("posts", posts);

        } catch (Exception e) {
            log.error("게시글 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "게시글을 불러오는데 실패했습니다.");
        }

        return "posts/list";
    }

    @GetMapping("/{id}/view")
    public String postDetail(@PathVariable Long id, Model model) {
        try {
            PostDto post = postService.getPostDetail(id);

            if (post == null) {
                model.addAttribute("error", "게시글을 찾을 수 없습니다.");
                return "posts/list";
            }

            model.addAttribute("post", post);
            model.addAttribute("comments", post.getComments());

        } catch (Exception e) {
            log.error("게시글 상세 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "게시글을 불러오는데 실패했습니다.");
        }

        return "posts/detail";
    }

    @GetMapping("/create")
    public String createPostForm(@RequestParam(required = false) String date,
                                 @RequestParam(required = false) String mealType,
                                 Model model,
                                 HttpSession session) {

        UserDto currentUser = (UserDto) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        PostRequestDto postRequest = new PostRequestDto();
        if (currentUser != null) {
            postRequest.setAuthor(currentUser.getUsername());
        }

        model.addAttribute("postRequest", postRequest);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedMealType", mealType);

        return "posts/create";
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute("postRequest") PostRequestDto postRequest,
                             BindingResult bindingResult,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String mealType,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        UserDto currentUser = (UserDto) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedMealType", mealType);
            return "posts/create";
        }

        try {
            String sessionId = (String) session.getAttribute("SESSION_ID");
            PostDto createdPost = postService.createPost(postRequest, date, mealType, sessionId);

            if (createdPost != null) {
                redirectAttributes.addFlashAttribute("success", "게시글이 작성되었습니다.");
                return "redirect:/posts/" + createdPost.getId() + "/view";
            } else {
                model.addAttribute("error", "게시글 작성에 실패했습니다.");
                model.addAttribute("selectedDate", date);
                model.addAttribute("selectedMealType", mealType);
                return "posts/create";
            }

        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedMealType", mealType);
            return "posts/create";
        }
    }
}
