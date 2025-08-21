package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GrowingTextController {
    private static final int MAX_CHARS = 700;
    private static final double MAX_SCALE = 32.0;  // 最大倍率

    @GetMapping("/")
    public String showForm() {
        return "index";
    }

    /**
     * 指定されたstep回目の文字サイズ拡大タイミングの累積ビックリセット数を返す
     * 1回目(0): 2, 2回目(1): 3, 3回目(2): 5, 4回目(3): 6, 5回目(4): 8 ... 以降 1,2交互に増加
     */
    private int getIncreasePoint(int step) {
        int point = 2;
        for (int i = 1; i <= step; i++) {
            point += (i % 2 == 1) ? 1 : 2;
        }
        return point;
    }

    @PostMapping("/grow")
    public String processText(@RequestParam("inputText") String inputText, Model model) {
        if (inputText.length() > MAX_CHARS) {
            model.addAttribute("error", "700文字以内で入力してください。");
            return "index";
        }

        // テキストを行単位に分割
        String[] lines = inputText.split("\\n", -1);

        StringBuilder html = new StringBuilder("<html><body style='font-family:Meiryo;'>");

        int exMarkCount = 0; // 累積ビックリセット
        int scaleStep = 0;   // 何回拡大したか

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            int lineLength = line.length();

            // この行のビックリマークセット数をカウント（連続ビックリは1回）
            int lineExMarkSets = 0;
            for (int i = 0; i < lineLength; i++) {
                char c = line.charAt(i);
                if (c == '!' || c == '！') {
                    lineExMarkSets++;
                    while (i + 1 < lineLength && (line.charAt(i + 1) == '!' || line.charAt(i + 1) == '！')) {
                        i++;
                    }
                }
            }

            exMarkCount += lineExMarkSets;

            // 拡大step計算（何回拡大？）
            while (exMarkCount >= getIncreasePoint(scaleStep)) {
                scaleStep++;
            }

            double currentScale = 1.0;
            for (int j = 0; j < scaleStep; j++) {
                currentScale *= 2.0;
                if (currentScale > MAX_SCALE) {
                    currentScale = MAX_SCALE;
                    break;
                }
            }

            html.append("<span style='font-size:").append(currentScale * 100).append("%;'>");
            for (int i = 0; i < lineLength; i++) {
                char c = line.charAt(i);
                String escapedChar = String.valueOf(c)
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace(" ", "&nbsp;");
                html.append(escapedChar);
            }
            html.append("</span>");
            if (lineIndex < lines.length - 1) {
                html.append("<br>");
            }
        }

        html.append("</body></html>");
        model.addAttribute("resultHtml", html.toString());
        return "result";
    }
}
