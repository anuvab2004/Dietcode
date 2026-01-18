@echo off
setlocal enabledelayedexpansion

echo ====================================
echo Java Bytecode Dead Code Analyzer
echo ====================================
echo.

set PROJECT_ROOT=%cd%
echo Project root: %PROJECT_ROOT%
echo.

set MVN_CMD="F:\IntelliJ IDEA 2025.3.1\plugins\maven\lib\maven3\bin\mvn.cmd"
echo Using IntelliJ Maven at: %MVN_CMD%
echo.

echo [1/4] Compiling the analyzer...
call %MVN_CMD% clean compile
if errorlevel 1 (
    echo Maven compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!
echo.

echo [2/4] Creating comprehensive test samples...
if exist test-classes rmdir /s /q test-classes
mkdir test-classes

REM Use a helper approach to create files without nested parentheses issues
call :CreateTestFiles
if errorlevel 1 (
    echo Failed to create test files!
    pause
    exit /b 1
)

cd test-classes
echo.
echo Compiling test classes...
javac *.java 2>nul
if errorlevel 1 (
    echo Failed to compile test classes! Showing errors:
    javac *.java
    cd ..
    pause
    exit /b 1
)

echo Test compilation successful!
echo Created classes:
dir *.class
echo.

cd ..

echo [3/4] Setting up dependencies...
set ASM_JAR=%USERPROFILE%\.m2\repository\org\ow2\asm\asm\9.6\asm-9.6.jar
set ASM_TREE_JAR=%USERPROFILE%\.m2\repository\org\ow2\asm\asm-tree\9.6\asm-tree-9.6.jar

if not exist "%ASM_JAR%" (
    echo ERROR: ASM JAR not found at: %ASM_JAR%
    echo Please run: mvn dependency:resolve
    pause
    exit /b 1
)

echo Found ASM libraries.
echo.

echo [4/4] Running dead code analyzer...
echo ====================================
echo Analysis Results
echo ====================================
echo.

set "CP=%PROJECT_ROOT%\target\classes;%ASM_JAR%;%ASM_TREE_JAR%;%PROJECT_ROOT%\test-classes"

echo Analyzing directory: %PROJECT_ROOT%\test-classes
echo Classpath configured
echo.

echo Starting analysis...
echo -----------------------------------------------------------------

java -cp "%CP%" org.example.Main "%PROJECT_ROOT%\test-classes"

set EXIT_CODE=%ERRORLEVEL%
echo -----------------------------------------------------------------

echo.
if %EXIT_CODE% EQU 0 (
    echo Analysis completed successfully!
) else (
    echo Analysis failed with error code: %EXIT_CODE%
)

echo.
echo Test files created in: %PROJECT_ROOT%\test-classes
echo.

pause
exit /b 0

:CreateTestFiles
REM Create SimpleTest.java
echo public class SimpleTest { > test-classes\SimpleTest.java
echo     public static void main(String[] args) { >> test-classes\SimpleTest.java
echo         liveMethod(); >> test-classes\SimpleTest.java
echo     } >> test-classes\SimpleTest.java
echo. >> test-classes\SimpleTest.java
echo     public static void liveMethod() { >> test-classes\SimpleTest.java
echo         System.out.println("Live method called"); >> test-classes\SimpleTest.java
echo     } >> test-classes\SimpleTest.java
echo. >> test-classes\SimpleTest.java
echo     public static void deadMethod() { >> test-classes\SimpleTest.java
echo         System.out.println("This method is never called"); >> test-classes\SimpleTest.java
echo     } >> test-classes\SimpleTest.java
echo. >> test-classes\SimpleTest.java
echo     private void privateDeadMethod() { >> test-classes\SimpleTest.java
echo         System.out.println("Private dead method"); >> test-classes\SimpleTest.java
echo     } >> test-classes\SimpleTest.java
echo } >> test-classes\SimpleTest.java
echo Created SimpleTest.java

REM Create ReflectionTest.java
echo import java.lang.reflect.Method; > test-classes\ReflectionTest.java
echo. >> test-classes\ReflectionTest.java
echo public class ReflectionTest { >> test-classes\ReflectionTest.java
echo     public static void main(String[] args) throws Exception { >> test-classes\ReflectionTest.java
echo         // Direct call >> test-classes\ReflectionTest.java
echo         normalMethod(); >> test-classes\ReflectionTest.java
echo. >> test-classes\ReflectionTest.java
echo         // Reflection call >> test-classes\ReflectionTest.java
echo         Class^<^?^> clazz = Class.forName("ReflectionTest"); >> test-classes\ReflectionTest.java

echo         Method method = clazz.getMethod("reflectionMethod"); >> test-classes\ReflectionTest.java
echo         method.invoke(null); >> test-classes\ReflectionTest.java
echo     } >> test-classes\ReflectionTest.java
echo. >> test-classes\ReflectionTest.java
echo     public static void normalMethod() { >> test-classes\ReflectionTest.java
echo         System.out.println("Normal method via direct call"); >> test-classes\ReflectionTest.java
echo     } >> test-classes\ReflectionTest.java
echo. >> test-classes\ReflectionTest.java
echo     public static void reflectionMethod() { >> test-classes\ReflectionTest.java
echo         System.out.println("Method called via reflection"); >> test-classes\ReflectionTest.java
echo     } >> test-classes\ReflectionTest.java
echo. >> test-classes\ReflectionTest.java
echo     public static void neverCalledReflection() { >> test-classes\ReflectionTest.java
echo         System.out.println("This reflection method is never called"); >> test-classes\ReflectionTest.java
echo     } >> test-classes\ReflectionTest.java
echo } >> test-classes\ReflectionTest.java
echo Created ReflectionTest.java

REM Create FieldTest.java
echo public class FieldTest { > test-classes\FieldTest.java
echo     // Used field >> test-classes\FieldTest.java
echo     private static int usedField = 100; >> test-classes\FieldTest.java
echo. >> test-classes\FieldTest.java
echo     // Unused field >> test-classes\FieldTest.java
echo     private static String unusedField = "Never accessed"; >> test-classes\FieldTest.java
echo. >> test-classes\FieldTest.java
echo     // Field used only in dead method >> test-classes\FieldTest.java
echo     private static int fieldInDeadCode = 200; >> test-classes\FieldTest.java
echo. >> test-classes\FieldTest.java
echo     public static void main(String[] args) { >> test-classes\FieldTest.java
echo         System.out.println("Used field value: " + usedField); >> test-classes\FieldTest.java
echo     } >> test-classes\FieldTest.java
echo. >> test-classes\FieldTest.java
echo     public static void deadMethod() { >> test-classes\FieldTest.java
echo         System.out.println("Dead method accessing field: " + fieldInDeadCode); >> test-classes\FieldTest.java
echo     } >> test-classes\FieldTest.java
echo } >> test-classes\FieldTest.java
echo Created FieldTest.java

REM Create ComplexTest.java
echo public class ComplexTest { > test-classes\ComplexTest.java
echo     public static void main(String[] args) { >> test-classes\ComplexTest.java
echo         System.out.println("Main method"); >> test-classes\ComplexTest.java
echo         methodA(); >> test-classes\ComplexTest.java
echo     } >> test-classes\ComplexTest.java
echo. >> test-classes\ComplexTest.java
echo     public static void methodA() { >> test-classes\ComplexTest.java
echo         System.out.println("Method A"); >> test-classes\ComplexTest.java
echo         methodB(); >> test-classes\ComplexTest.java
echo     } >> test-classes\ComplexTest.java
echo. >> test-classes\ComplexTest.java
echo     public static void methodB() { >> test-classes\ComplexTest.java
echo         System.out.println("Method B"); >> test-classes\ComplexTest.java
echo         // Conditional logic >> test-classes\ComplexTest.java
echo         if (false) { >> test-classes\ComplexTest.java
echo             deadBlock(); >> test-classes\ComplexTest.java
echo         } >> test-classes\ComplexTest.java
echo     } >> test-classes\ComplexTest.java
echo. >> test-classes\ComplexTest.java
echo     public static void deadBlock() { >> test-classes\ComplexTest.java
echo         System.out.println("This block is never executed"); >> test-classes\ComplexTest.java
echo     } >> test-classes\ComplexTest.java
echo. >> test-classes\ComplexTest.java
echo     public static void completelyDead() { >> test-classes\ComplexTest.java
echo         System.out.println("Completely dead method"); >> test-classes\ComplexTest.java
echo     } >> test-classes\ComplexTest.java
echo } >> test-classes\ComplexTest.java
echo Created ComplexTest.java

exit /b 0