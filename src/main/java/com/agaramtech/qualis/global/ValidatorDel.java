package com.agaramtech.qualis.global;


import org.springframework.stereotype.Component;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * This class is used to hold the delete validation failure messages.
 */
//@Lazy
@Component
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ValidatorDel {

	public int nreturnstatus;
	public String sreturnmessage;

}
