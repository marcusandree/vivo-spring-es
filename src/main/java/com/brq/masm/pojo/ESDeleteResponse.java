package com.brq.masm.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ESDeleteResponse {
	@JsonProperty("_index")
	String index;
	@JsonProperty("_type")
	String type;
	@JsonProperty("_id")
	String id;
	@JsonProperty("_version")
	int version;
	@JsonProperty("_result")
	String result;
	@JsonProperty("_shards")
	Shard shards;
	@JsonProperty("_seq_no")
	int seq_no;
	@JsonProperty("_primary_term")
	int primary_term;
}	

@Getter
@Setter
@NoArgsConstructor
class Shard {
	int total;
	int successful;
	int failed;
}

