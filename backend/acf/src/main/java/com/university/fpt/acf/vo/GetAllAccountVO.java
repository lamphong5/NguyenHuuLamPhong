package com.university.fpt.acf.vo;

import com.university.fpt.acf.config.security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;


@Data
@AllArgsConstructor
public class GetAllAccountVO {
    private Long id;
    private String username;
    private Long idRole;
    private String nameRole;
    private Boolean status;
    private LocalDate time;
}
