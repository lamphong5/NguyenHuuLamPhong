package com.university.fpt.acf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailVO {
    private String image;
    private String fullName;
    private Boolean gender;
    private LocalDate dob;
    private String email;
    private String phone;
    private String address;
    private String nation;
    private Long salary;
    private Long idPosition;
    private String positionName;
}
