package com.lexer.demo;

class Token {
	
	TokenType type;
	String value;
	
	Token(TokenType type, String value) {
		
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return type + "(" + value + ")";
	}
	
	

}
