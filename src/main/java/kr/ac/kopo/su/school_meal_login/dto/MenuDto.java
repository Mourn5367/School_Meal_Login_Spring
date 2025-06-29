package kr.ac.kopo.su.school_meal_login.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {
    private Long id;
    private String date;

    // 개별 식사 타입
    @JsonProperty("meal_type")
    private String mealType; // "아침", "점심", "저녁"

    private String content; // 해당 식사의 메뉴 내용

    private String breakfast;
    private String lunch;
    private String dinner;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
