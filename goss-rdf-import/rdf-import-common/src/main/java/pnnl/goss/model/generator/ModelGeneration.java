package pnnl.goss.model.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ModelGeneration {
	
	/**
	 * All generated classes will be under this root.
	 */
	private static final String ROOT_PACKAGE = "pnnl.goss.cim";
	
	/**
	 * The root of the java source folder.
	 */
	private static final String ROOT_FOLDER = "src/main/java";
	
	private static final Integer CLASSES_PACKAGE_COLUMN = 0;
	private static final Integer CLASSES_CLASS_COLUMN = 1;
	
	private static Map<String, MetaClass> metaClasses = new HashMap<>();
	
	/**
	 * Creates a package directory under ROOT_FOLDER.
	 * 
	 * If the folder already exists then it is not created.
	 * 
	 * @param classPackage A package that should be created.
	 * @return The directory that was attempted to make
	 */
	public static String createPackageDir(String classPackage){
		System.out.println("Creating package: "+classPackage);
		String packageDir = classPackage.replace(".", "/");
		Path dir = Paths.get(ROOT_FOLDER, packageDir);

		// Already exists
		if (dir.toFile().isDirectory()){
			return dir.toAbsolutePath().toString();
		}
		
		if (!dir.toFile().mkdirs()){
			System.out.println("Failed to create: "+dir.toAbsolutePath());
			return null;
		}
		return dir.toString(); //.toAbsolutePath().toString();
	}
	
	/**
	 * Gets the string of the package that a class should be in.  The package is expected
	 * to be from the first column of the Classes worksheet in the xls file.
	 * 
	 * @param data Data in the cell of the first column of the classes tab in the xls file.
	 * @return The package or null if not available.
	 */
	public static String getPackage(String data){
		String clspackage = ROOT_PACKAGE;
		
		if(data.startsWith("ETXSCADA")){
			clspackage += ".ext.scada";
		}
		else if(data.startsWith("CIMSCADA")){
			clspackage += ".scada";
		}
		else if(data.startsWith("CIM") || data.startsWith("EXT")){
			
			if (data.startsWith("EXT")){
				clspackage += "ext";
			}
			
			for(int i=3; i < data.length();i++){
				if(Character.isUpperCase(data.charAt(i))){
					clspackage += "."+Character.toString(Character.toLowerCase(data.charAt(i)));
				}
				else{
					clspackage += data.charAt(i);
				}
			}
		}
		else{
			clspackage = null;
		}
		
		return clspackage;
	}

	/**
	 * Starts the generation of the models that are in the xls file
	 * 
	 * @param existingFile The downloaded xls file from ERCOT.
	 * @throws IOException 
	 */
	public static void generateModels(File existingFile) throws IOException{
		System.out.println("Generating models ...");
		HSSFWorkbook wb = readFile(existingFile);
		
		HSSFSheet classesSheet = wb.getSheet("Classes");
		
		// First row is header
		for(int r=1; r < classesSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = classesSheet.getRow(r);
			if (row == null) {
				continue;
			}
			HSSFCell pkgCell = row.getCell(CLASSES_PACKAGE_COLUMN);
			if (pkgCell != null){
				if (pkgCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
					System.out.println("Skipping: " +pkgCell.getNumericCellValue());
				}
				else if(pkgCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
					// Use this as a test so that we can get the mapped type and see
					// if it's already in the collection (Hopefully it never will!)
					MetaClass tmp = new MetaClass();
					tmp.setPackageName(getPackage(pkgCell.getStringCellValue()));
					tmp.setClassName(row.getCell(CLASSES_CLASS_COLUMN).getStringCellValue());
					
					if (!tmp.isValidClassDefinition()){
						System.out.println("Invalid class detected for row: "+ r);
					}
					else{
						MetaClass newMetaClass = metaClasses.get(tmp.getDataType());
						if (newMetaClass == null){
							newMetaClass = tmp;
							metaClasses.put(newMetaClass.getDataType(), newMetaClass);
						}
						else{
							System.out.println("Boo already has the datatype!! "+newMetaClass.getDataType());
						}
					}
				}
			}
		}
		
		// Loop and make .java files from the class meta files.
		for(MetaClass meta: metaClasses.values()){
			String packageDir = createPackageDir(meta.getPackageName());
			File classFile = new File(packageDir);
			
			try{
				String fullJavaFilePath = classFile.toString() + "/" + meta.getClassName()+".java";
				System.out.println("Creating java file: "+fullJavaFilePath);
				FileWriter writer = new FileWriter(fullJavaFilePath);
				BufferedWriter out = new BufferedWriter(writer);
				out.write(meta.getClassDefinition());
				out.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		System.out.println("Generation complete");
	}
	
	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 */
	private static HSSFWorkbook readFile(File xlsFile) throws IOException {
		return new HSSFWorkbook(new FileInputStream(xlsFile));
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		
		// Root of the resources path at src/main/resources
		URL url = ModelGeneration.class.getClassLoader().getResource("ERCOT_DataDictionary_1.7.xls");
		File file = new File(url.toURI());
		if (file.exists()){
			generateModels(file);
		}

	}

}
