package com.trecapps.falsehoods.submit.controllers;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Login {

    String username, password;

}
