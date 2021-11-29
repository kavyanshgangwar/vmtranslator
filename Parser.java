import java.util.*;
/**
 * The Parser class parses the vm code 
 * 
 * @author Kavyansh Gangwar
 * */
class Parser{
	// the default instance of Parser to be used
	public static final Parser instance = new Parser();

	/**
	 * Sole constructor to be used to create objects in this class only
	 * */
	private Parser(){}

	/**
	 * gets code from the file
	 * 
	 * gets the code from the file and processes it in a more consumable way
	 * 
	 * @param filename the name of the file from which code is to be read
	 * 
	 * @return ArrayList of code
	 * */
	public ArrayList<ArrayList<String> > parse(String fileName){
		// get code from the file
		String program = FileReader.instance.readFile(fileName);
		// split the lines of code
		String[] lines = program.split("\n");
		ArrayList<ArrayList<String> > commands = new ArrayList<ArrayList<String> > ();
		// split the words of code
		for(int i=0;i<lines.length;i++){
			commands.add(new ArrayList<String>(Arrays.asList(lines[i].split(" "))));
		}
		return commands;
	}
}