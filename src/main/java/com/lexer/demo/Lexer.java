package com.lexer.demo;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
	
	private final String input;
	private int pos = 0;
	
	Lexer(String input) {
		this.input = input;
	}
	
	List<Token> tokenize() {
		List<Token> tokens = new ArrayList<>();
		
		while (pos < input.length()) {
			char c = input.charAt(pos);
			
			// Skip whitespace
			if (Character.isWhitespace(c)) {
				pos++;
				continue;
			}
			
			// Single-character tokens
			if(c == '|') {
				tokens.add(new Token(TokenType.PIPE, "|"));
				pos++;
				continue;
			}
			
			if(c == '>') {
				tokens.add(new Token(TokenType.REDIRECT_OUT, ">"));
				pos++;
				continue;
			}
			
			if (c == '&') {
				tokens.add(new Token(TokenType.BACKGROUND, "&"));
				pos++;
				continue;
			}
			
			// Word token
			StringBuilder word = new StringBuilder();
			while (pos < input.length()) {
				c = input.charAt(pos);
				if (Character.isWhitespace(c) || "|>$".indexOf(c) != -1) {
					break;
				}
				word.append(c);
				pos++;
			}
			
			tokens.add(new Token(TokenType.WORD, word.toString()));
			
			
		}
		
		tokens.add(new Token(TokenType.EOF, ""));
		return tokens;
	}

}
