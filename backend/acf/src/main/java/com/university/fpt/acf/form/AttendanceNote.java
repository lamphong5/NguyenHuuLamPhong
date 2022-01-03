package com.university.fpt.acf.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceNote {
    @NotBlank(message = "id cannot be blank")
    private Long id;
    @NotNull(message = "note cannot be null")
    private String note;
}
