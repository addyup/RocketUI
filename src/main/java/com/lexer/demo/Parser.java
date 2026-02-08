package com.lexer.demo;

import java.util.List;

public class Parser {
	private final List<Token> tokens;
	private int pos = 0;
	
	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
	public static PipelineNode parseSingleCommand(String command) {
    Lexer lexer = new Lexer(command);
    List<Token> tokens = lexer.tokenize();
    Parser parser = new Parser(tokens);
    return parser.parse();
}

	
	public PipelineNode parse() {
		PipelineNode pipeline = new PipelineNode();
		CommandNode current = new CommandNode();
		
		while(!peek(TokenType.EOF)) {
			if(peek(TokenType.WORD)) {
				
				Token t = consume(TokenType.WORD);
				if(current.name == null) {
					current.name = t.value;
				}
				else {
					current.args.add(t.value);
				}
				
				
			}
			else if (peek(TokenType.PIPE)) {
				consume(TokenType.PIPE);
				pipeline.commands.add(current);
				current = new CommandNode();
				
			}
			else if (peek(TokenType.REDIRECT_OUT)) {
				consume(TokenType.REDIRECT_OUT);
				current.redirectOut = consume(TokenType.WORD).value;
			}
			else if (peek(TokenType.BACKGROUND)) {
				consume(TokenType.BACKGROUND);
				current.background = true;
			}
			else {
				throw new RuntimeException("Unexpected token: " + peek());
			}
			
		}
		
		pipeline.commands.add(current);
		return pipeline;
	}
	
	private boolean peek(TokenType type) {
		return tokens.get(pos).type == type;
	}
	private Token peek() {
	    return tokens.get(pos);
	}

	
	private Token consume(TokenType type) {
		Token t = tokens.get(pos);
		if(t.type != type) {
			throw new RuntimeException("Expected "+ type + " but got " + t.type);
		}
		pos++;
		return t;
	}

}
