package com.trecapps.falsehoods.submit.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegionEntry {

	Region region;
	
	String contents;

	List<Record> records;

}
