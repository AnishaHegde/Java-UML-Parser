import com.sun.javadoc.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.ArrayList;

public class JavaUMLParser{
	
    public static boolean start(RootDoc root) {
		try {
			ClassDoc[] classes = root.classes();
			
	// Retrieve output file name mentioned in command line argument
			String[][] nameOfFile = root.options();
			String plantUmlFile = nameOfFile[5][1] + ".txt";
			String sourceCodePath = nameOfFile[6][1] + "\\";
			
	// Retrieve package name.
			PackageDoc packageName = classes[0].containingPackage();
			
			File inputFile = new File(plantUmlFile);
			FileOutputStream outputFileStr = new FileOutputStream(inputFile);
			PrintWriter printW = new PrintWriter(outputFileStr);
			
	// Start PlantUML grammar
			printW.println("@startuml");
			printW.println("skinparam classAttributeIconSize 0");
			if (!(packageName.name().equals("")))
				printW.println("package " + packageName.name() + " <<Folder>> {");
						
			ArrayList<String> interfaceClassList = new ArrayList<String>();
			ArrayList<String> interfacesToExclude = new ArrayList<String>();
			ArrayList<String> onlyClassesList = new ArrayList<String>();
			ArrayList<String> classesToExcludeList = new ArrayList<String>();
			ArrayList<String> allClassesList = new ArrayList<String>();
			ArrayList<String> methodsList = new ArrayList<String>();
			ArrayList<String> methodGetterSetterList = new ArrayList<String>();
			ArrayList<String> usesList = new ArrayList<String>();

			for (int i = 0; i < classes.length; ++i) {
				allClassesList.add(classes[i].name());
				if (classes[i].isInterface())
					interfaceClassList.add(classes[i].name());
				else
					onlyClassesList.add(classes[i].name());
			}
						
			for (int i = 0; i < classes.length; ++i) {
				
		// Check if Class retrived is an Interface
				boolean isInterface = classes[i].isInterface();
				
				/*if (!isInterface) {
					// Retrieve the list of Interfaces implemented by this class.
					Type[] interfaceRealized = classes[i].interfaceTypes();
					if (interfaceRealized.length != 0) {
						for (int m=0; m < interfaceRealized.length; m++) {
							printW.println(interfaceRealized[m].asClassDoc().name() + " <|.. " + classes[i].name());
						}
					}
				}
				
				if (isInterface)
					printW.println("class " + classes[i].name() + " <<interface>> {");
				else if (classes[i].isAbstract())
					printW.println("abstract class " + classes[i].name() + "{");
				else
					printW.println("class " + classes[i].name() + "{");*/
				
		// Retrieve and print constructors
				ConstructorDoc[] constructors = classes[i].constructors();
				
				for (int z=0; z < constructors.length; z++){
					Parameter[] parameters = constructors[z].parameters();
				
					for (int l=0; l < parameters.length; l++) 
					{
						if(!(parameters[l].type().typeName().toString().equals("String"))) 
						{
							String classDependency = classes[i].name() + ":" + parameters[l].type().typeName();
							String classDependencyRev = parameters[l].type().typeName() + ":" + classes[i].name();
						
							if (!(usesList.contains(classDependency)) || !(usesList.contains(classDependencyRev))) 
							// Conditions to check for both interface and class
								if (allClassesList.contains(parameters[l].type().typeName())) 
								{	
									if(!(classes[i].isInterface()))
									{	
										printW.println(classes[i].name() + " ..> " + parameters[l].type().typeName() + " : uses");
										usesList.add(classDependency);
										usesList.add(classDependencyRev);
									}
									else
									{
										if(!(interfaceClassList.contains(parameters[l].type().typeName().toString())))
										{
											printW.println(classes[i].name() + " ..> " + parameters[l].type().typeName() + " : uses");
											usesList.add(classDependency);
											usesList.add(classDependencyRev);
										}
									}			
								}//End if which is checking for Class or Interface declaration
								else if(!(parameters[l].type().isPrimitive())) 
								{
									String javaSourceFile = sourceCodePath + classes[i].name() + ".java";
									String genericTypeString = getGenericType(javaSourceFile, parameters[l].name());
									
									if (allClassesList.contains(genericTypeString)) 
									{								
										if(!(classes[i].isInterface()) && !(interfaceClassList.contains(parameters[l].type().typeName().toString())))
										{	
											printW.println(classes[i].name() + " ..> " + genericTypeString + " : uses");
											usesList.add(classDependency);
											usesList.add(classDependencyRev);	
										}
									}
								}
						}
					}// End loop for parameters
				}// End for loop to print constructors
				
			
				// Retrieve Interface and Class Attributes
				FieldDoc[] attributes = classes[i].fields();
				for (int k=0; k < attributes.length; k++) 
				{
					
					if(attributes[k].isPublic() || attributes[k].isPrivate())
					{
						
						if(!(attributes[k].type().typeName().toString().equals("String"))) 
						{
							// Conditions to check for both interface and class attributes
							if (allClassesList.contains(attributes[k].type().typeName())) 
							{
								String classDependency = classes[i].name() + ":" + attributes[k].type().typeName();
								String classDependencyRev = attributes[k].type().typeName() + ":" + classes[i].name();
								
								
								//if(!isInterface && !(interfaceClassList.contains(attributes[k].type().typeName())))
								if(!(isInterface && (interfaceClassList.contains(attributes[k].type().typeName()))))
								
									if (!(classesToExcludeList.contains(classDependency)) || !(classesToExcludeList.contains(classDependencyRev))
										|| !(interfacesToExclude.contains(classDependency)) || !(interfacesToExclude.contains(classDependencyRev))) 
									{
										
										if ((attributes[k].type().dimension().toString()).equals(""))
											printW.print(attributes[k].type().typeName() + " \"1\" " + " --");
										else
											printW.print(attributes[k].type().typeName() + " \"*\" " + " --");
										
										//Check for dependency in other classes to prevent multiplicity from printing twice
										boolean isSecondClassPairSet = false;
										for (int i1 = 0; i1 < classes.length; ++i1) {
											if (classes[i1].name().equals(attributes[k].type().typeName())) {
												FieldDoc[] attributes1 = classes[i1].fields();
												for (int k1=0; k1 < attributes1.length; k1++) {
													if(attributes1[k1].isPublic() || attributes1[k1].isPrivate()){
														if ((attributes1[k1].type().typeName()).equals(classes[i].name())) {
															isSecondClassPairSet = true;
															if ((attributes1[k1].type().dimension().toString()).equals(""))
																printW.println(" \"1\" " + classes[i].name());
															else
																printW.println(" \"*\" " + classes[i].name());
														}
													}
												}
											}
										}
										if (!isSecondClassPairSet) {
											printW.println(" " + classes[i].name());
										}
											
										classesToExcludeList.add(classDependency);
										classesToExcludeList.add(classDependencyRev);
										interfacesToExclude.add(classDependency);
										interfacesToExclude.add(classDependencyRev);
									}
							}//End if which is checking for Class or Interface declaration 
							else if(!(attributes[k].type().isPrimitive())) 
							{
								String javaSourceFile = sourceCodePath + classes[i].name() + ".java";
								String genericTypeString = getGenericType(javaSourceFile, attributes[k].name());
								
								if (allClassesList.contains(genericTypeString)) 
								{
									String classDependency = classes[i].name() + ":" + genericTypeString;
									String classDependencyRev = genericTypeString + ":" + classes[i].name();
								
								
									if (!(classesToExcludeList.contains(classDependency)) || !(classesToExcludeList.contains(classDependencyRev))
										|| !(interfacesToExclude.contains(classDependency)) || !(interfacesToExclude.contains(classDependencyRev))) 
									{
										
										printW.print(genericTypeString + " \"*\" " + " --");
										boolean isSecondClassPairSet = false;
										for (int i1 = 0; i1 < classes.length; ++i1) {
											if (classes[i1].name().equals(genericTypeString)) {
												FieldDoc[] attributes1 = classes[i1].fields();
												for (int k1=0; k1 < attributes1.length; k1++) {
													if(attributes1[k1].isPublic() || attributes1[k1].isPrivate()){
														if ((attributes1[k1].type().typeName()).equals(classes[i].name())) {
															isSecondClassPairSet = true;
															if ((attributes1[k1].type().dimension().toString()).equals("")) {
																printW.println(" \"1\" " + classes[i].name());
															}
															else {
																printW.println(" \"*\" " + classes[i].name());
															}
														}
													}
												}
											}
										}
										if (!isSecondClassPairSet) {
											printW.println(" " + classes[i].name());
										}
											
										classesToExcludeList.add(classDependency);
										classesToExcludeList.add(classDependencyRev);
										interfacesToExclude.add(classDependency);
										interfacesToExclude.add(classDependencyRev);
									}
								}//End if to check allClassesList contains generic Class or Interface	
							}//End else which is checking for generic type of attribute declaration
						}//End if for checking attibute type is not a String	
					}//End if for checking for public and private attributes	
				}//End for loop to attributes iteration
				
				
				// Retrieve method names, return type and arguments
				MethodDoc[] methods = classes[i].methods();
				
				for (int j=0; j < methods.length; j++) 
				{
					Parameter[] parameters = methods[j].parameters();
					for (int l=0; l < parameters.length; l++) 
					{
						if(!(parameters[l].type().typeName().toString().equals("String"))) 
						{	
							String classDependency = classes[i].name() + ":" + parameters[l].type().typeName();
							String classDependencyRev = parameters[l].type().typeName() + ":" + classes[i].name();
						
						if (!(usesList.contains(classDependency)) || !(usesList.contains(classDependencyRev))) 
							// Conditions to check for both interface and class
							if (allClassesList.contains(parameters[l].type().typeName())) 
							{	
								if(!(classes[i].isInterface()))
								{	
									printW.println(classes[i].name() + " ..> " + parameters[l].type().typeName() + " : uses");
									usesList.add(classDependency);
									usesList.add(classDependencyRev);
								}
								else
								{
									if(!(interfaceClassList.contains(parameters[l].type().typeName().toString())))
									{
										printW.println(classes[i].name() + " ..> " + parameters[l].type().typeName() + " : uses");
										usesList.add(classDependency);
										usesList.add(classDependencyRev);
									}
								}			
							}//End if which is checking for Class or Interface declaration
							else if(!(parameters[l].type().isPrimitive())) 
							{
								String javaSourceFile = sourceCodePath + classes[i].name() + ".java";
								String genericTypeString = getGenericType(javaSourceFile, parameters[l].name());
								
								if (allClassesList.contains(genericTypeString)) 
								{								
									if(!(classes[i].isInterface()) && !(interfaceClassList.contains(parameters[l].type().typeName().toString())))
									{	
										printW.println(classes[i].name() + " ..> " + genericTypeString + " : uses");
										usesList.add(classDependency);
										usesList.add(classDependencyRev);	
									}
								}
							}//End else if which is checking for Genereic Type in parameters
						}//End if for checking attibute type is not a String
					}// End of parameters loop
					
					String javaSourceFile = sourceCodePath + classes[i].name() + ".java";
					String returnValue = checkWithinClass(javaSourceFile, methods[j].name(), allClassesList);
					
					if (!(returnValue.equals("")))
						if (interfaceClassList.contains(returnValue)) {
							String interfaceDependency = classes[i].name() + ":" + returnValue;
							if (!(interfacesToExclude.contains(interfaceDependency))) {
								printW.println(classes[i].name() + " ..> " + returnValue + " : uses");
								interfacesToExclude.add(interfaceDependency);
							}
						}						
						else if (onlyClassesList.contains(returnValue))
							printW.println(classes[i].name() + " -- " + returnValue);
						
				}// End for loop to print methods

				
		// Retrive subclasses to print Inheritance
				Type classType = classes[i].superclassType();
				if (classType != null && !((classType.asClassDoc().name()).equals("Object"))) {
					printW.println(classType.asClassDoc().name() + " <|-- " + classes[i].name());
				}
				
				
				if (!isInterface) {
		// Retrieve all the Interfaces implemented by this class
					Type[] interfaceRealized = classes[i].interfaceTypes();
					if (interfaceRealized.length != 0) {
						for (int m=0; m < interfaceRealized.length; m++) {
							printW.println(interfaceRealized[m].asClassDoc().name() + " <|.. " + classes[i].name());
						}
					}
				}
				
	//Start of class/interface in PlantUML grammar
				if (isInterface)
					printW.println("class " + classes[i].name() + " <<interface>> {");
				else if (classes[i].isAbstract())
					printW.println("abstract class " + classes[i].name() + "{");
				else
					printW.println("class " + classes[i].name() + "{");
				
		//For printing Attributes		
				for (int k=0; k < attributes.length; k++) 
				{
									
					// Java attributes as "setters and getters"	
					if (attributes[k].isPrivate()) {
						String getterMethod = "get" + attributes[k].name().toString();
						String setterMethod = "set" + attributes[k].name().toString();
						
						// Put getter and setter methods in an ArrayList to be excluded while printing
						for (int j=0; j < methods.length; j++) {
							if (methods[j].isPublic()){
								if((methods[j].name().equalsIgnoreCase(getterMethod)) || (methods[j].name().equalsIgnoreCase(setterMethod)))
									methodGetterSetterList.add(methods[j].name());
							}	
						}
						
						//ArrayList<String> = checkGetterSetter();
						boolean isAttributePrinted = false;
						for (int j=0; j < methods.length; j++) {
							if((methods[j].name().equalsIgnoreCase(getterMethod)) || (methods[j].name().equalsIgnoreCase(setterMethod)))
							{
								printW.println("+" + attributes[k].name() + ":" + attributes[k].type().typeName());
								isAttributePrinted = true;
								break;
							}
						}	
						if (!isAttributePrinted)
						{
							if (!((attributes[k].type().typeName()).equals("ArrayList") ||
								(attributes[k].type().typeName()).equals("List") ||
								(attributes[k].type().typeName()).equals("Collection"))) {
								if (!(attributes[k].type().isPrimitive()))
									if (attributes[k].isStatic())
										printW.println("{static} -" + attributes[k].name() + ":" + attributes[k].type().typeName() + attributes[k].type().dimension());
									else
										printW.println("-" + attributes[k].name() + ":" + attributes[k].type().typeName() + attributes[k].type().dimension());
								else
									if (attributes[k].isStatic())
										printW.println("{static} -" + attributes[k].name() + ":" + attributes[k].type());
									else
										printW.println("-" + attributes[k].name() + ":" + attributes[k].type());
							}
						}
					}
					
					// Print only public attributes
					if((attributes[k].isPublic())) {
						if (!((attributes[k].type().typeName()).equals("ArrayList") ||
							(attributes[k].type().typeName()).equals("List") ||
							(attributes[k].type().typeName()).equals("Collection"))) {
							if (!(attributes[k].type().isPrimitive()))
								if (attributes[k].isStatic())
									printW.println("{static} +" + attributes[k].name() + ":" + attributes[k].type().typeName() + attributes[k].type().dimension());
								else
									printW.println("+" + attributes[k].name() + ":" + attributes[k].type().typeName() + attributes[k].type().dimension());
							else
								if (attributes[k].isStatic())
									printW.println("{static} +" + attributes[k].name() + ":" + attributes[k].type());
								else
									printW.println("+" + attributes[k].name() + ":" + attributes[k].type());
						}
					}
				}
				
		//For printing constructors
				for (int z=0; z < constructors.length; z++){
					
					// To print constructores mentioned explicitly that is exclude default constructors
					String javaSourceFile = sourceCodePath + classes[i].name() + ".java";
					boolean includeConstructor = false;
					boolean constructExists = checkExplicitConstructor(javaSourceFile, constructors[z].name());
				
					if (constructExists && constructors[z].isPublic())
						includeConstructor = true;
						
					Parameter[] parameters = constructors[z].parameters();
					if (!((constructors[z].isPublic()) && (parameters.length == 0)) || includeConstructor) {
						if (constructors[z].isPublic() || constructors[z].isPrivate()) {
							if (constructors[z].isPublic())
								if (constructors[z].isStatic())
									printW.print("{static} +" + constructors[z].name() + "(");
								else	
									printW.print("+" + constructors[z].name() + "(");
							if (constructors[z].isPrivate())
								if (constructors[z].isStatic())
									printW.print("{static} -" + constructors[z].name() + "(");
							else	
								printW.print("-" + constructors[z].name() + "(");	

							for (int l=0; l < parameters.length; l++) {
								if (!((parameters[l].type().typeName()).equals("ArrayList") ||
									(parameters[l].type().typeName()).equals("List") ||
									(parameters[l].type().typeName()).equals("Collection"))) {
										if (l != parameters.length - 1)
											if (!(parameters[l].type().isPrimitive()))
												printW.print(parameters[l].name() + ":" + parameters[l].type().typeName() + parameters[l].type().dimension() + ", ");
											else
												printW.print(parameters[l].name() + ":" + parameters[l].type() + ", ");
										else
											if (!(parameters[l].type().isPrimitive()))
												printW.print(parameters[l].name() + ":" + parameters[l].type().typeName() + parameters[l].type().dimension());
											else
												printW.print(parameters[l].name() + ":" + parameters[l].type());
								}
							}
							printW.println(")");
						}
					}
				}
				
				
		//For printing methods		
				for (int j=0; j < methods.length; j++) {
					methodsList.add(methods[j].name());
				}
				
				for (int j=0; j < methods.length; j++) {
					
					if (!methodGetterSetterList.contains(methods[j].name()))
					{		
						Parameter[] parameters = methods[j].parameters();
				
							if (methods[j].isPublic()) {
								if (methods[j].isStatic())
									printW.print("{static} +" + methods[j].name() + "(");
								else if (methods[j].isAbstract())
									printW.print("{abstract} +" + methods[j].name() + "(");
								else
									printW.print("+" + methods[j].name() + "(");

							for (int l=0; l < parameters.length; l++) {
								
								if (!((parameters[l].type().typeName()).equals("ArrayList") ||
								(parameters[l].type().typeName()).equals("List") ||
								(parameters[l].type().typeName()).equals("Collection"))) {
									if (l != parameters.length - 1)
										if (!(parameters[l].type().isPrimitive()))
											printW.print(parameters[l].name() + ":" + parameters[l].type().typeName() + parameters[l].type().dimension() + ", ");
										else
											printW.print(parameters[l].name() + ":" + parameters[l].type() + ", ");
										else
											if (!(parameters[l].type().isPrimitive()))
												printW.print(parameters[l].name() + ":" + parameters[l].type().typeName() + parameters[l].type().dimension());
											else
												printW.print(parameters[l].name() + ":" + parameters[l].type());
								}
							}
							if (methods[j].returnType().isPrimitive())
								printW.println("):" + methods[j].returnType());
							else
								printW.println("):" + methods[j].returnType().typeName());
						}
					}
				}// for loop on array methods 
	 
				printW.println("}"); // closing brace for class in PlantUML grammar
				printW.flush();
			} //for loop on array of classes.
			if (!(packageName.name().equals("")))
				printW.println("}"); // closing brace for package
			printW.println("@enduml");
			printW.flush();
			outputFileStr.close();
			printW.close();
			
		} //End try	in class Parser
		catch(IOException e){}
		return true;
    }// End of start() method
	
	public static String getGenericType(String fileName, String objectName) {
	
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String str = null;
			str = br.readLine();
			if (objectName.equals("b"))
				objectName = objectName + ";";
			if (objectName.equals("a"))
				objectName = objectName + ";";
			while (str != null && str != "") {
				if (str.contains(objectName)) {
					str = str.substring(str.indexOf("<")+1,str.indexOf(">"));
					return str;
				}
				str = br.readLine();
			}
		}
		catch(Exception e) {
		}
		return "";
	}
	
	public static String checkWithinClass(String filename, String methodName, ArrayList<String> onlyClassesList) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String strLine = null;
			
			while ((strLine = br.readLine()) != null) {
				if (strLine.contains(methodName)) {
					do {
						strLine = br.readLine();
						for (int i=0; i < onlyClassesList.size(); i++)
							if (strLine.contains(onlyClassesList.get(i)) && !(strLine.contains("System.out")))
								return onlyClassesList.get(i);
					} while((strLine != null) || !(strLine.contains("}")));
				}
				//strLine = br.readLine();
			}
		}
		catch(Exception e) {}
		return "";
	}
	
	/*public static ArrayList<String> checkGetterSetter(String attributeName, ArrayList<String> allMethods) 
	{
			String getterMethod = "get" + attributeName;
			String setterMethod = "set" + attributeName;
			ArrayList<String> tempList = new ArrayList<String>();
			boolean isGetterSetter = false;
			
			for (int j=0; j < allMethods.size(); j++) 
			{
				if((allMethods.contains(getterMethod)) || (allMethods.contains(setterMethod)))
				{
					tempList.add(allMethods.get(i));
					isGetterSetter = true;
				}
					
			}
		if(isGetterSetter)
			return tempList;
		else
			return "";
	}*/
	
	public static boolean checkExplicitConstructor(String fileName, String consName) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String str = null;
			//str = br.readLine();
			
			String consName1 = consName + "()";
			String consName2 = consName + " ()";
			while ((str = br.readLine()) != null) {
				if (str.contains(consName1) || str.contains(consName2)) {
					return true;
				}
			//str = br.readLine();	
			}
		}
		catch(Exception e) {
		}
		return false;
		
    }
	
	public static void main(String args[]) {
        try {
			String sourcePath = args[0] + "\\*";
			String outputFile = args[1] + ".txt";
			Process javaDoc = Runtime.getRuntime().exec("javadoc -doclet JavaUMLParser -docletpath . -private " + sourcePath + " -sourcepath " + args[1] + " -subpackages " + args[0]);
			javaDoc.waitFor();
			
			Process uml = Runtime.getRuntime().exec("java -jar plantuml.jar " + outputFile);
			uml.waitFor();
		}// try in main
		catch(Exception e){}
	
	} // End main() method
} // Class Parser