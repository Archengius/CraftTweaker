package zenscript.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zenscript.IZenErrorLogger;
import zenscript.lexer.Token;
import zenscript.lexer.ZenTokener;
import static zenscript.lexer.ZenTokener.*;
import zenscript.parser.elements.ParsedFunction;
import zenscript.parser.statement.ParsedStatement;
import zenscript.util.StringUtil;
import zenscript.util.ZenPosition;

/**
 * Contains a parsed file.
 *
 * A parsed file contains:
 * <ul>
 * <li>A set of imports</li>
 * <li>A set of parsed functions</li>
 * <li>A set of statuments</li>
 * </ul>
 *
 * This parsed file cannot be executed by itself, but it can be compiled into a
 * module, possibly together with other files.
 *
 * @author Stan Hebben
 */
public class ParsedFile
{
	private final ParsedModule module;
	private final String filename;

	private final ParsedPackage _package;
	private final List<ParsedImport> imports;
	private final Map<String, ParsedFunction> functions;
	private final List<ParsedStatement> statements;

	/**
	 * Constructs and parses a given file.
	 *
	 * @param module module this file is part of
	 * @param filename parsed filename
	 * @param tokener input tokener
	 */
	public ParsedFile(ParsedModule module, String filename, ZenTokener tokener)
	{
		this.filename = filename;
		this.module = module;

		imports = new ArrayList<ParsedImport>();
		functions = new HashMap<String, ParsedFunction>();
		statements = new ArrayList<ParsedStatement>();

		tokener.setFile(this);

		_package = parsePackage(tokener);
		loadFileContents(tokener);
	}

	/**
	 * Gets the input filename for this file.
	 *
	 * @return input filename
	 */
	public String getFileName()
	{
		return filename;
	}

	/**
	 * Gets the imports list.
	 *
	 * @return imports list
	 */
	public List<ParsedImport> getImports()
	{
		return imports;
	}

	/**
	 * Gets this file's script statements.
	 *
	 * @return script statement list
	 */
	public List<ParsedStatement> getStatements()
	{
		return statements;
	}

	/**
	 * Gets the functions defined inside this file.
	 *
	 * @return script functions
	 */
	public Map<String, ParsedFunction> getFunctions()
	{
		return functions;
	}

	// #############################
	// ### Object implementation ###
	// #############################
	@Override
	public String toString()
	{
		return filename;
	}

	// #######################
	// ### Private methods ###
	// #######################
	private void tryLoadFileContents(ZenPosition position, String filename)
	{
		InputStream file = module.loadFile(StringUtil.unescapeString(filename));
		if (file == null) {
			module.getErrorLogger().error(
					position,
					"Could not load file " + filename
			);
		} else {
			try {
				loadFileContents(ZenTokener.fromInputStream(file));
			} catch (IOException ex) {
				module.getErrorLogger().error(
						position,
						"Could not load file " + filename
				);
			}
		}
	}

	private void loadFileContents(ZenTokener tokener)
	{
		while (tokener.hasNext()) {
			Token next = tokener.peek();
			switch (next.getType()) {
				case T_IMPORT:
					imports.add(ParsedImport.parse(tokener, module.getErrorLogger()));
					break;

				case T_INCLUDE:
					readInclude(tokener);
					break;

				case T_CLASS:
					parseClass(tokener);
					break;

				case T_INTERFACE:
					parseInterface(tokener);
					break;

				case T_ENUM:
					parseEnum(tokener);
					break;

				case T_STRUCT:
					parseStruct(tokener);
					break;

				case T_EXPAND:
					parseExpansion(tokener);
					break;

				case T_FUNCTION:
					parseFunction(tokener, module.getErrorLogger());
					break;

				default:
					statements.add(ParsedStatement.parse(tokener, module.getErrorLogger()));
					break;
			}
		}
	}

	private void readInclude(ZenTokener tokener)
	{
		// process "include" token
		tokener.next();

		Token includeFileName = tokener.required(T_STRINGVALUE, "string literal expected");
		tokener.required(T_SEMICOLON, "; expected");

		tryLoadFileContents(includeFileName.getPosition(), includeFileName.getValue());
	}

	private void parseClass(ZenTokener tokener)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void parseInterface(ZenTokener tokener)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void parseEnum(ZenTokener tokener)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void parseStruct(ZenTokener tokener)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void parseExpansion(ZenTokener tokener)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void parseFunction(ZenTokener tokener, IZenErrorLogger errorLogger)
	{
		ParsedFunction function = ParsedFunction.parse(tokener, errorLogger);

		if (functions.containsKey(function.getName())) {
			module.getErrorLogger().error(function.getPosition(), "function " + function.getName() + " already exists");
		} else {
			functions.put(function.getName(), function);
		}
	}

	private static ParsedPackage parsePackage(ZenTokener tokener)
	{
		if (tokener.optional(T_PACKAGE) == null) {
			return null;
		}

		List<String> name = tokener.parseIdentifierDotSequence();
		tokener.required(T_SEMICOLON, "; expected");

		return new ParsedPackage(StringUtil.join(name, "."));
	}
}
