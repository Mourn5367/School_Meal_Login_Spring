package kr.ac.kopo.su.school_meal_login.controller;

import kr.ac.kopo.su.school_meal_login.dto.*;
import kr.ac.kopo.su.school_meal_login.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long postId,
                                @RequestParam String content,
                                @RequestParam String author,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        UserDto currentUser = (UserDto) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            String sessionId = (String) session.getAttribute("SESSION_ID");

            CommentRequestDto commentRequest = new CommentRequestDto();
            commentRequest.setContent(content);
            commentRequest.setAuthor(currentUser.getUsername());

            CommentDto createdComment = commentService.createComment(postId, commentRequest, sessionId);

            if (createdComment != null) {
                redirectAttributes.addFlashAttribute("success", "댓글이 작성되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "댓글 작성에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "서버 오류가 발생했습니다.");
        }

        return "redirect:/posts/" + postId + "/view";
    }

    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long postId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        UserDto currentUser = (UserDto) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            String sessionId = (String) session.getAttribute("SESSION_ID");

            boolean deleted = commentService.deleteComment(commentId, sessionId);

            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "댓글이 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "댓글 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "서버 오류가 발생했습니다.");
        }

        return "redirect:/posts/" + postId + "/view";
    }
}