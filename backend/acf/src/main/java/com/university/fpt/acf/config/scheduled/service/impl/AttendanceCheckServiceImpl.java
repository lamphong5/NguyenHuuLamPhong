package com.university.fpt.acf.config.scheduled.service.impl;

import com.university.fpt.acf.config.scheduled.service.AttendanceCheckService;
import com.university.fpt.acf.entity.Employee;
import com.university.fpt.acf.entity.TimeKeep;
import com.university.fpt.acf.repository.AccountManagerRepository;
import com.university.fpt.acf.repository.AttendanceRepository;
import com.university.fpt.acf.repository.EmployeeCustomRepository;
import com.university.fpt.acf.vo.GetAllEmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceCheckServiceImpl implements AttendanceCheckService {
    @Autowired
    private EmployeeCustomRepository employeeCustomRepository;

    @Autowired
    private AccountManagerRepository accountManagerRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Value("${acf.scross.path}")
    private String path;

    @Override
    public void checkAttendance() {
        //************************************
        // Check attendance of employees in combination with sending mail
        // 1. Compare employee information and attendance information. If the employee does not have attendance information, configure the mail
        // 2. Transfer the list of non-attendant employees to email content to send reports via email to admin
        //************************************
        try {
            LocalDateTime dateTime = LocalDateTime.now();
            int count = 0;
            if (dateTime.isAfter(LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 18, 40))) {
                count = 3;
            } else if (dateTime.isAfter(LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 18, 25))) {
                count = 2;
            } else if (dateTime.isAfter(LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 18, 10))) {
                count = 1;
            }
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String dateTimeAfterFormat = dateTime.format(dateTimeFormat);

            List<GetAllEmployeeVO> getAllEmployeeVOList = employeeCustomRepository.getAllEmployeeNotAttendanceJob();

            if (getAllEmployeeVOList != null && getAllEmployeeVOList.size() != 0) {
                List<String> listEmail = accountManagerRepository.getEmailAdmin();

                for (String s : listEmail) {
                    MimeMessage mimeMessage = emailSender.createMimeMessage();
                    MimeMessageHelper helper =
                            new MimeMessageHelper(mimeMessage, "utf-8");
                    helper.setText(this.buildEmail(getAllEmployeeVOList, path + "/#/attendance"), true);
                    helper.setTo(s);
                    helper.setSubject((count == 0 ? "" : "[Nh???c l???i l???n " + count + "]") + "Th?? nh???c nh??? ch???m c??ng ng??y " + dateTimeAfterFormat);
                    emailSender.send(mimeMessage);
                }
            }
        } catch (MessagingException e) {
            throw new IllegalStateException("failed to send email");
        }
    }

    @Override
    public void autoAttendance() {
        //************************************
        // Automatic attendance for employees
        // 1. Search all employees who do not have attendance information
        // 2. Make timekeeping for employees
        //************************************
        try {
            List<GetAllEmployeeVO> getAllEmployeeVOList = employeeCustomRepository.getAllEmployeeNotAttendanceJob();
            List<TimeKeep> timeKeeps = new ArrayList<>();
            for (GetAllEmployeeVO getAllEmployeeVO : getAllEmployeeVOList) {
                TimeKeep timeKeep = new TimeKeep();
                timeKeep.setCreated_by("JOB_AUTO");
                timeKeep.setModified_by("JOB_AUTO");
                timeKeep.setDate(LocalDate.now());
                timeKeep.setType("0");
                timeKeep.setNote("??i???m danh t??? ?????ng");
                Employee employee = new Employee();
                employee.setId(getAllEmployeeVO.getId());
                timeKeep.setEmployee(employee);
                timeKeeps.add(timeKeep);
            }
            attendanceRepository.saveAll(timeKeeps);
        } catch (Exception ex) {
            throw new IllegalStateException("Kh??ng th??? ch???y t??? ?????ng");
        }

    }

//    private String buildEmail(List<GetAllEmployeeVO> getAllEmployeeVOList, String link) {
//        //************************************
//        // Function build content mail
//        //************************************
//        LocalDateTime dateTime = LocalDateTime.now();
//        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
//
//        String dateTimeAfterFormat = dateTime.format(dateTimeFormat);
//
//        StringBuilder sql = new StringBuilder("");
//        sql.append("<div style=\" width:80%; margin: 0 auto;\">\n" +
//                "        <img src=\"\">\n" +
//                "        <table style=\"width:100%;\">\n" +
//                "            <tr>\n" +
//                "                <td colspan=\" 2 \">Xin ch??o, </td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "                <td>Hi???n t???i h??? th???ng c?? ki???m tra ph???n ch???m c??ng mu???n th??ng b??o ?????n b???n m???t s??? th??ng tin:</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "                <td style=\"padding-left: 30px;\">- Th???i gian: " + dateTimeAfterFormat + "</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "                <td style=\"padding-left: 30px;\">- H??? th???ng c??: " + getAllEmployeeVOList.size() + " ng?????i ch??a ???????c ch???m c??ng</td>\n" +
//                "            </tr>\n" +
//                "        </table>\n" +
//                "        <br>\n" +
//                "        <table style=\" width:100%; border:1px solid black; margin: 0 auto;\">\n" +
//                "<tr>\n" +
//                "                <th style=\"border:1px solid black; \">S??? th??? t???</th>\n" +
//                "                <th style=\"border:1px solid black; \">H??? v?? t??n</th>\n" +
//                "                <th style=\"border:1px solid black; \">Tr???ng th??i</th>\n" +
//                "            </tr>\n");
//
//        for (GetAllEmployeeVO getAllEmployeeVO : getAllEmployeeVOList) {
//            sql.append("            <tr>\n" +
//                    "                <td style=\"border:1px solid black; \">" + getAllEmployeeVO.getId() + "</td>\n" +
//                    "                <td style=\"border:1px solid black; \">" + getAllEmployeeVO.getName() + "</td>\n" +
//                    "                <td style=\"border:1px solid black; \">Ch??a ch???m c??ng</td>\n" +
//                    "            </tr>\n");
//        }
//        sql.append("        </table>\n" +
//                "\n" +
//                "        <br>\n" +
//                "        <p>????? ngh??? b???n ???n v??o link b??n d?????i ????? <strong style=\"color: red; font-size: 25px; \">ch???m c??ng ngay</strong> cho c??c nh??n vi??n ????.</p>\n" +
//                "        <button style=\"display: block; margin-left: auto; margin-right: auto; background-color: #40A9FF; color: white;\">" +
//                "      <a href=\"" + link + "\">Ch???m c??ng!</a></button>\n" +
//                "        <p>Tr??n tr???ng,</p>\n" +
//                "        <h3 style=\"font-family: 'Courier New', Courier, monospace \">Anh Chung Furniture</h3>\n" +
//                "    </div>");
//        return sql.toString();
//    }

    private String buildEmail(List<GetAllEmployeeVO> getAllEmployeeVOList, String link) {
        //************************************
        // Function build content mail
        //************************************
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

        String dateTimeAfterFormat = dateTime.format(dateTimeFormat);

        StringBuilder sql = new StringBuilder("");
        sql.append("    <div style=\"max-width: 680px; \">\n" +
                "        <table width=\"100%\">\n" +
                "            <tr>\n" +
                "                <td style=\"background-color:#ffffff;padding:0 10px 10px;text-align:left\">\n" +
                "                    <table width=\"100%\">\n" +
                "                        <tr>\n" +
                "                            <td style=\"padding: 10px;font-family: 'Arial', sans-serif;font-size:14px; line-height: 21px;color:#000000;\">\n" +
                "                                <p style=\"margin:0;\">\n" +
                "                                    <strong>\n" +
                "                                        Xin ch??o,\n" +
                "                                        <p style=\"color:#000000\">Hi???n t???i h??? th???ng c?? ki???m tra ph???n ch???m c??ng mu???n th??ng\n" +
                "                                            b??o ?????n b???n m???t s??? th??ng tin:</p>\n" +
                "                                        <ul>\n" +
                "                                            <li>\n" +
                "                                                Th???i gian: " + dateTimeAfterFormat + "\n" +
                "                                            </li>\n" +
                "                                            <li>\n" +
                "                                                H??? th???ng c??: " + getAllEmployeeVOList.size() + " ng?????i ch??a ???????c ch???m c??ng\n" +
                "                                            </li>\n" +
                "                                        </ul>\n" +
                "                                    </strong>\n" +
                "                                </p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"padding: 5px;\">\n" +
                "                    <table border=\"1\" style=\"margin: auto; width: 100%; text-align: center;\">\n" +
                "                        <thead>\n" +
                "                            <tr>\n" +
                "                                <th>S??? th??? t???</th>\n" +
                "                                <th>H??? v?? t??n</th>\n" +
                "                                <th>Tr???ng th??i</th>\n" +
                "                            </tr>\n" +
                "                        </thead>\n" +
                "                        <tbody>");

        for (GetAllEmployeeVO getAllEmployeeVO : getAllEmployeeVOList) {
            sql.append("            <tr>\n" +
                    "                <td style=\"border:1px solid black; \">" + getAllEmployeeVO.getId() + "</td>\n" +
                    "                <td style=\"border:1px solid black; \">" + getAllEmployeeVO.getName() + "</td>\n" +
                    "                <td style=\"border:1px solid black; \">Ch??a ch???m c??ng</td>\n" +
                    "            </tr>\n");
        }
        sql.append("</tbody>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"padding: 5px;background-color:#ffffff;\">\n" +
                "                    <table style=\"margin: auto;\">\n" +
                "                        <tr>\n" +
                "                            <p style=\"text-align:center;\">\n" +
                "                                <strong>\n" +
                "                                    <p style=\"color:#3c8491\">\n" +
                "                                        ????? ngh??? b???n ???n v??o link b??n d?????i ????? <span style=\"font-size: 22px; color: red;\">ch???m c??ng ngay</span> cho c??c nh??n vi??n ????.\n" +
                "                                    </p>\n" +
                "                                </strong>\n" +
                "                            </p>\n" +
                "                        </tr>\n" +
                "                        <tr>\n" +
                "                            <td style=\"border-radius: 30px; background: #00B5AD;text-align:center\">\n" +
                "                                <a href=\"" + link + "\" style=\"border: 1px solid #00B5AD; font-family: Arial,sans-serif; font-size:14px;line-height: 14px; text-decoration: none; padding: 10px 31px; color: #ffffff; font-weight:bold;display: block; border-radius: 30px;\"><strong>\n" +
                "                                        Ch???m c??ng</strong></a>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "\n" +
                "            <tr>\n" +
                "                <td>\n" +
                "                    <div style=\"background-color:#ffffff;margin: 0 -1px; width:100%; max-width:680px; vertical-align:top;\" class=\"cc-column\">\n" +
                "                        <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\n" +
                "                            <tr>\n" +
                "                                <td style=\"padding-top:0; padding-left: 10px; font-family: Arial, sans-serif; font-size:14px;color: #0C2340;\">\n" +
                "                                    Tr??n tr???ng,\n" +
                "                                    <h4> C??ng Ty N???i Th???t Anh Chung</h4>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td height=\"1\" style=\"line-height:2px; font-size:2px; background-color: #0C2340;\">\n" +
                "                                    &nbsp;\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </div>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"padding: 10px; font-family: Arial, sans-serif; font-size:14px; color: #0C2340;background-color:#ffffff;\">\n" +
                "                    <table style=\" font-family: 'Oxygen', sans-serif; font-size: 14px;\">\n" +
                "                        <tr>\n" +
                "                            <td style=\"width: 50vh\">\n" +
                "                                <img src=\"https://i.postimg.cc/m25nfzcj/logo.jpg\" alt=\"\" style=\"width:250px;\">\n" +
                "                            </td>\n" +
                "                            <td style=\"width: 50vh\">\n" +
                "                                <h3>C??ng Ty N???i Th???t Anh Chung </h3>\n" +
                "                                <p>\n" +
                "                                    ?????a ch???: Th??n 10, Kim Quan, Th???ch Th???t, H?? N???i\n" +
                "                                </p>\n" +
                "                                <p>\n" +
                "                                    S??? ??i???n tho???i: 0975 446 588\n" +
                "                                </p>\n" +
                "                                <p>\n" +
                "                                    Email: levanchung1312@gmail.com\n" +
                "                                </p>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </div>");
        return sql.toString();
    }
}
