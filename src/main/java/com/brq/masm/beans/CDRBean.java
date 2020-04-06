package com.brq.masm.beans;

import com.brq.masm.pojo.CDR;
import com.brq.masm.pojo.RecordID;
import com.brq.masm.pojo.Call;
import com.brq.masm.utils.CPF;

public class CDRBean {
	public RecordID createRecordID(String id) {
		return new RecordID(id);
	}
	
	public Call createCallFromCDR(CDR cdr) {
		CPF cpf = new CPF();
		Call c = new Call();
		
		c.setCtn_orig(cdr.getCtn_orig());
		c.setCtn_dest(cdr.getCtn_dest());
		c.setDuration(cdr.getDuration());
		c.setTimestamp(cdr.getTimestamp());
    	c.setClientCPF(cpf.getCPF(cdr.getCtn_orig(), cdr.getTimestamp()));
    	
    	return c;
	}
	
	public String createStringFromESResult (org.elasticsearch.action.DocWriteResponse.Result r) {
		return r.toString();
	}
	
}
