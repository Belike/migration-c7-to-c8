package com.camunda.consulting.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VacationRequest {
    private String firstName;
    private String lastName;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @NotNull
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @NotNull
    private Date endDate;
}
