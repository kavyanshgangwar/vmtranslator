import java.io.*;
import java.util.*;
/**
 * The VMTranslator class is used to translate the vm code to asm code
 * 
 * @author Kavyansh Gangwar
 * */
class VMTranslator{

	/**
	 * Sole constructor to stop the creation of objects of this class
	 * */
	private VMTranslator(){}
	
	/**
	 * drives the whole translator
	 * 
	 * @param args contains name of file to be translated
	 * 
	 * */
	public static void main(String[] args){
		if(args.length==0){
			System.out.println("The program needs a file as an argument.");
			return ;
		}

		String fileName = args[0].trim();
		String asmFileName;
		String curFileName;
		ArrayList<String> fileList=new ArrayList<String>();

		// check if the argument is a single file 
		if(fileName.trim().substring(fileName.length()-3,fileName.length()).equals(".vm")){
			String temp[] = fileName.split("/");
			fileName="";
			for(int i=0;i<temp.length-1;i++){
				if(i!=0){
					fileName+="/";
				}
				fileName+=temp[i];
			}
			asmFileName = temp[temp.length-1].trim();
			asmFileName = asmFileName.substring(0,asmFileName.length()-3);
			fileList.add(temp[temp.length-1].trim());
		}else{
			File directoryPath = new File(fileName);
			String contents[] = directoryPath.list();
			for(int i=0;i<contents.length;i++){
				if(contents[i].trim().substring(contents[i].trim().length()-3,contents[i].trim().length()).equals(".vm")){
					fileList.add(contents[i].trim());
				}
			}
			String temp[] = fileName.split("/");
			asmFileName=temp[temp.length-1];
		}
		// create a new file to write the output
		CodeWriter.instance.initialize(fileName+"/"+asmFileName+".asm");
		CodeWriter.instance.writeBuffer("// initialization code\n");
		CodeWriter.instance.writeBuffer(initializationCode());

		// read all the files and parse the files
		for(int k=0;k<fileList.size();k++){
			// parsed commands for current file;
			ArrayList< ArrayList<String> > commands = Parser.instance.parse(fileName+"/"+fileList.get(k));
			curFileName = fileList.get(k).trim().substring(0,fileList.get(k).trim().length()-3);
			
			for(int i=0;i<commands.size();i++){
				// write the command to file to know what vm code is the
				// following asm code for
				String  command = "// ";
				for(int j=0;j<commands.get(i).size();j++){
					command += commands.get(i).get(j)+" ";
				}
				CodeWriter.instance.writeBuffer(command + "\n");
				// write the asm code to buffer
				CodeWriter.instance.writeBuffer(translate(commands.get(i),i,curFileName));
			}
		}
		// write the buffer to the file
		CodeWriter.instance.write();
	}

	private static String initializationCode(){
		// initialize stack pointer
		String codeInAsm = "@261\nD=A\n@SP\nM=D\n";
		// initialize ARG pointer
		codeInAsm += "@Sys.init\n0;JMP\n";
		return codeInAsm;

	}

	/**
	 * translates vm commands into asm code
	 * 
	 * @param command the command that is to be translated
	 * @param i the line number of command
	 * @param fileName the name of the file which is being translated
	 * 
	 * @return asm code for the vm command
	 * */
	private static String translate(ArrayList<String> command,int i,String fileName){
		String codeInAsm="";

		// handles arithematic and logical operations
		
			switch(command.get(0)){
				case "add": 
				codeInAsm += handleAdd();
				break;
				case "sub":
				codeInAsm += handleSub();
				break;
				case "neg":
				codeInAsm += handleNeg();
				break;
				case "eq":
				codeInAsm += handleEq(i,fileName);
				break;
				case "gt":
				codeInAsm += handleGt(i,fileName);
				break;
				case "lt":
				codeInAsm += handleLt(i,fileName);
				break;
				case "and":
				codeInAsm += handleAnd();
				break;
				case "or":
				codeInAsm += handleOr();
				break;
				case "not":
				codeInAsm += handleNot();
				break;
				case "push":
				codeInAsm += handlePush(command,fileName);
				break;
				case "pop":
				codeInAsm += handlePop(command,fileName);
				break;
				case "label":
				codeInAsm += handleLabel(command,fileName);
				break;
				case "goto":
				codeInAsm += handleGoto(command,fileName);
				break;
				case "if-goto":
				codeInAsm += handleIfGoto(command,fileName);
				break;
				case "function":
				codeInAsm += handleFunction(command);
				break;
				case "call":
				codeInAsm += handleCall(command,i);
				break;
				case "return":
				codeInAsm += handleReturn(command);
				break;
			}
		
		return codeInAsm;
	}

	/**
	 * gives asm code for adding two numbers
	 * 
	 * generated the code for adding the two topmost element in the stack and 
	 * saves their result to the stack
	 * 
	 * @return string code for adding two numbers
	 * */ 
	private static String handleAdd(){
		String codeInAsm = "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nM=M+D\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for substracting two numbers
	 * 
	 * generated the code for substracting the two topmost element (top2-top1)
	 * in the stack and saves their result to the stack
	 * 
	 * @return string code for substracting two numbers
	 * */ 
	private static String handleSub(){
		String codeInAsm = "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nM=M-D\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives asm code to negate a number
	 * 
	 * generates the code for changing the sign of the topmost number of the stack
	 * 
	 * @return String code for negating a number
	 * */
	private static String handleNeg(){
		String codeInAsm = "@SP\nAM=M-1\nM=-M\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for checking equality
	 * 
	 * generated the code to check if two pop most numbers of stack are equal or not
	 * and save the result (true(-1)/false(0)) on the stack
	 * 
	 * @param i the line number of the command without comments
	 * @param fileName the name of the which the command belongs to
	 * 
	 * @return String code to check equality of two topmost numbers of stack
	 * */
	private static String handleEq(int i,String fileName){
		String codeInAsm = "";
		// checks for numbers are equal
		codeInAsm += "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nMD=M-D\n@label"+fileName+i+"false\nD ; JNE\n";
		// if ans is true
		codeInAsm += "(label"+fileName+i+"true)\n";
		codeInAsm += "@SP\nA=M\nM=-1\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// if ans is false
		codeInAsm += "(label"+fileName+i+"false)\n";
		codeInAsm += "@SP\nA=M\nM=0\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// end of the check
		codeInAsm += "(label"+fileName+i+"end)\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for checking which of the two numbers is big
	 * 
	 * generated the code to check if top second numbers of stack is greater than the top most
	 * number of the stack or not and save the result (true(-1)/false(0)) on the stack
	 * 
	 * @param i the line number of the command without comments
	 * @param fileName the name of the which the command belongs to
	 * 
	 * @return String asm code for checking which of the two numbers is big
	 * */
	private static String handleGt(int i,String fileName){
		String codeInAsm = "";
		codeInAsm += handleSub();
		// check greater than
		codeInAsm += "@SP\nAM=M-1\nD=M\n@label"+fileName+i+"true"+"\nD ; JGT\n";
		// if ans is false
		codeInAsm += "(label"+fileName+i+"false)\n";
		codeInAsm += "@SP\nA=M\nM=0\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// if ans is true
		codeInAsm += "(label"+fileName+i+"true)\n";
		codeInAsm += "@SP\nA=M\nM=-1\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// end of check
		codeInAsm += "(label"+fileName+i+"end)\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for checking which of the two numbers is small
	 * 
	 * generated the code to check if top second numbers of stack is smaller than the top most
	 * number of the stack or not and save the result (true(-1)/false(0)) on the stack
	 * 
	 * @param i the line number of the command without comments
	 * @param fileName the name of the which the command belongs to
	 * 
	 * @return String asm code for checking which of the two numbers is small
	 * */
	private static String handleLt(int i,String fileName){
		String codeInAsm = "";
		codeInAsm += handleSub();
		// check lesser than
		codeInAsm += "@SP\nAM=M-1\nD=M\n@label"+fileName+i+"true"+"\nD ; JLT\n";
		// if ans is false
		codeInAsm += "(label"+fileName+i+"false)\n";
		codeInAsm += "@SP\nA=M\nM=0\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// if ans is true
		codeInAsm += "(label"+fileName+i+"true)\n";
		codeInAsm += "@SP\nA=M\nM=-1\n@SP\nM=M+1\n@label"+fileName+i+"end\n0 ; JMP\n";
		// end of check
		codeInAsm += "(label"+fileName+i+"end)\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for anding two numbers
	 * 
	 * generated the code for anding the two topmost element in the stack and 
	 * saves their result to the stack
	 * 
	 * @return string code for anding two numbers
	 * */
	private static String handleAnd(){
		String codeInAsm = "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nM=M&D\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives asm code for oring two numbers
	 * 
	 * generated the code for oring the two topmost element in the stack and 
	 * saves their result to the stack
	 * 
	 * @return string code for oring two numbers
	 * */
	private static String handleOr(){
		String codeInAsm = "@SP\nAM=M-1\nD=M\n@SP\nAM=M-1\nM=M|D\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives asm code to not of a number
	 * 
	 * generates the code for computing the not of the topmost number of the stack
	 * 
	 * @return String code for not of a number
	 * */
	private static String handleNot(){
		String codeInAsm = "@SP\nAM=M-1\nM=!M\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * handles different pop commands
	 * 
	 * generated code for different pop commands like pop segment i
	 * 
	 * @param command the pop command for which code is to be generated
	 * @param fileName the name of the file to which the command belongs
	 * 
	 * @return String the code for pop command
	 * */
	private static String handlePop(ArrayList<String> command,String fileName){
		String codeInAsm = "";
		// gets the data in D register from the stack
		codeInAsm += "@SP\nAM=M-1\nD=M\n";
		// saves the data to the required location
		switch(command.get(1)){
			case "local":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@LCL\nD=M+D\n@13\nM=D\n@SP\nA=M\nD=M\n@13\nA=M\nM=D\n";
			break;
			case "argument":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@ARG\nD=M+D\n@13\nM=D\n@SP\nA=M\nD=M\n@13\nA=M\nM=D\n";
			break;
			case "this":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@THIS\nD=M+D\n@13\nM=D\n@SP\nA=M\nD=M\n@13\nA=M\nM=D\n";
			break;
			case "that":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@THAT\nD=M+D\n@13\nM=D\n@SP\nA=M\nD=M\n@13\nA=M\nM=D\n";
			break;
			case "static":
			codeInAsm += "@"+fileName+"."+command.get(2)+"\nM=D\n";
			break;
			case "temp":
			int addr = 5 + Integer.parseInt(command.get(2));
			codeInAsm += "@"+addr+"\nM=D\n";
			break;
			case "pointer":
			if(command.get(2).equals("0")){
				codeInAsm+="@THIS\nM=D\n";
			}else{
				codeInAsm+="@THAT\nM=D\n";
			}
			break;
		}
		return codeInAsm;
	}

	/**
	 * handles different push commands
	 * 
	 * generated code for different push commands like push segment i
	 * 
	 * @param command the push command for which code is to be generated
	 * @param fileName the name of the file that the command belongs to
	 * 
	 * @return String the code for push command
	 * */
	private static String handlePush(ArrayList<String> command,String fileName){
		String codeInAsm= "";
		// load data from segment to the D register
		switch(command.get(1)){
			case "local":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@LCL\nA=M+D\nD=M\n";
			break;
			case "argument":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@ARG\nA=M+D\nD=M\n";
			break;
			case "this":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@THIS\nA=M+D\nD=M\n";
			break;
			case "that":
			codeInAsm += "@"+command.get(2)+"\nD=A\n@THAT\nA=M+D\nD=M\n";
			break;
			case "constant":
			codeInAsm += "@"+command.get(2)+"\nD=A\n";
			break;
			case "static":
			codeInAsm += "@"+fileName+"."+command.get(2)+"\nD=M\n";
			break;
			case "temp":
			int addr = 5 + Integer.parseInt(command.get(2));
			codeInAsm += "@"+addr+"\nD=M\n";
			break;
			case "pointer":
			if(command.get(2).equals("0")){
				codeInAsm+="@THIS\nD=M\n";
			}else{
				codeInAsm+="@THAT\nD=M\n";
			}
			break;
		}
		// push the data into the stack
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		return codeInAsm;
	}

	/**
	 * gives the asm code for label
	 * 
	 * generate asm code for label command with a convention of label
	 * 
	 * @param command the label command that needs to be translated
	 * @param fileName the name of the file which the command belongs to
	 * 
	 * @return String asm code of the label command
	 * */
	private static String handleLabel(ArrayList<String> command,String fileName){
		return "("+fileName+"$"+command.get(1)+")\n";
	}

	/**
	 * gives the asm code for goto
	 * 
	 * generates the code for the goto command using the label convention
	 * 
	 * @param command the goto command that need to be translated
	 * @param fileName the name of the file which the command belongs to
	 * 
	 * @return String asm code of the goto command
	 * */
	private static String handleGoto(ArrayList<String> command,String fileName){
		String codeInAsm = "";
		codeInAsm+="@"+fileName+"$"+command.get(1)+"\n";
		codeInAsm+="0;JMP\n";
		return codeInAsm;
	}

	/**
	 * gives the asm code for if-goto
	 * 
	 * generates the code for the if-goto command using the label convention
	 * 
	 * @param command the goto command that need to be translated
	 * @param fileName the name of the file which the command belongs to
	 * 
	 * @return String asm code of the if-goto command
	 * */
	private static String handleIfGoto(ArrayList<String> command,String fileName){
		String codeInAsm = "";
		codeInAsm += "@SP\nAM=M-1\nD=M\n";
		codeInAsm += "@"+fileName+"$"+command.get(1)+"\n";
		codeInAsm += "D;JNE\n";
		return codeInAsm;
	}


	private static String handleCall(ArrayList<String> command,int lineNumber){
		String codeInAsm = "";
		// push retAddrLabel
		codeInAsm += "@"+command.get(1)+"$ret"+lineNumber+"\n";
		codeInAsm += "D=A\n";
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		// push LCL
		codeInAsm += "@LCL\n";
		codeInAsm += "D=M\n";
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		// push ARG
		codeInAsm += "@ARG\n";
		codeInAsm += "D=M\n";
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		// push THIS
		codeInAsm += "@THIS\n";
		codeInAsm += "D=M\n";
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		// push THAT
		codeInAsm += "@THAT\n";
		codeInAsm += "D=M\n";
		codeInAsm += "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		// ARG = SP-5-nArgs
		codeInAsm += "@SP\nD=M\n@5\nD=D-A\n@"+command.get(2)+"\nD=D-A\n";
		codeInAsm += "@ARG\nM=D\n";
		// LCL = SP
		codeInAsm += "@SP\nD=M\n";
		codeInAsm += "@LCL\nM=D\n";
		// goto functionName
		codeInAsm += "@"+command.get(1)+"\n";
		codeInAsm += "0;JMP\n";
		// retAddrLabel
		codeInAsm += "("+command.get(1)+"$ret"+lineNumber+")\n";
		return codeInAsm;
	}

	private static String handleFunction(ArrayList<String> command){
		String codeInAsm = "";
		// (fucntionName)
		codeInAsm += "("+command.get(1)+")\n";
		// repeat nVars times
		// push 0
		int nVars = Integer.parseInt(command.get(2));
		for(int i=0;i<nVars;i++){
			codeInAsm += "@SP\nA=M\nM=0\n@SP\nM=M+1\n";
		}
		return codeInAsm;
	}

	private static String handleReturn(ArrayList<String> command){
		String codeInAsm = "";
		// retAddr = *(LCL-5)
		codeInAsm += "@5\nD=A\n";
		codeInAsm += "@LCL\nA=M\nA=A-D\nD=M\n";
		codeInAsm += "@13\nM=D\n";
		// *ARG = pop()
		codeInAsm += "@SP\nAM=M-1\nD=M\n";
		codeInAsm += "@ARG\nA=M\nM=D\n";
		// SP = ARG+1
		codeInAsm += "@ARG\nD=M\n@SP\nM=D+1\n";
		// THAT = *(LCL-1)
		codeInAsm += "@LCL\nA=M-1\nD=M\n@THAT\nM=D\n";
		// THIS = *(LCL-2)
		codeInAsm += "@2\nD=A\n@LCL\nA=M-D\nD=M\n@THIS\nM=D\n";
		// ARG = *(LCL-3)
		codeInAsm += "@3\nD=A\n@LCL\nA=M-D\nD=M\n@ARG\nM=D\n";
		// LCL = *(LCL-4)
		codeInAsm += "@4\nD=A\n@LCL\nA=M-D\nD=M\n@LCL\nM=D\n";
		// goto retAddr
		codeInAsm += "@13\nA=M\n0;JMP\n";
		return codeInAsm;
	}
}