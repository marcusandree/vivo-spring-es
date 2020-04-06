package com.brq.masm.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CDR {
	private long ctn_orig;
	private long ctn_dest;
	private String timestamp;
	private long duration;
}
