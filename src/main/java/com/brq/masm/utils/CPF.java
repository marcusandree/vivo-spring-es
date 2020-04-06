package com.brq.masm.utils;

import java.util.Random;

/***
 * classe com metodos que simulam uma consulta a base de dados para 
 * definir a que cliente (identificado por um numero de CPF)
 * pertence um numero de telefone em determinada data. como trata-se
 * de simulacao, estamos respondendo aleatoriamente entre dois cpfs
 * diferentes.
 * Uma implementacao real faria uma conexao em algum tipo de banco de
 * dados e executaria uma query especifica
 */
public class CPF {
	public long getCPF(long ctn, String timestamp) {
		Random r = new java.util.Random();
		if (r.nextInt(2) == 1) {
			return 24918188842L;
		}
		return 24791662873L;
	}
}
