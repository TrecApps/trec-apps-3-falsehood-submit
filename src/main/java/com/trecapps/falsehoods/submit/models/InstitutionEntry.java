package com.trecapps.falsehoods.submit.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class InstitutionEntry {

	Institution institution;
	
	String contents;

	List<Record> records;

}
