package com.brq.masm.typeconverters;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import com.brq.masm.pojo.CDR;
import com.brq.masm.pojo.Call;
import com.brq.masm.utils.CPF;

public class CDR2Call implements TypeConverters {
    // @Converter
    public Call toCall(CDR cdr) {
    	Call c = new Call();
    	CPF cpf = new CPF();
    	c.setClientCPF(cpf.getCPF(cdr.getCtn_orig(), cdr.getTimestamp()));
        return c;
    }
}
