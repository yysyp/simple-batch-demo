package ps.demo.simplebatchdemo.dto;


import lombok.Data;

import java.util.Date;

@Data
public class StudentResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String zip;

    private Date dateOfBirth;

    private Double gpa;

    private Integer status;

}
