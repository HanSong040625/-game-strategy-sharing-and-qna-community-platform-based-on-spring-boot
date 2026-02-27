import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckAdminPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试BCrypt密码格式
        String testPassword = "admin123";
        String bcryptEncoded = encoder.encode(testPassword);
        
        System.out.println("BCrypt加密后的密码格式:");
        System.out.println(bcryptEncoded);
        System.out.println("长度: " + bcryptEncoded.length());
        System.out.println("以$2a$开头: " + bcryptEncoded.startsWith("$2a$"));
        System.out.println("以$2b$开头: " + bcryptEncoded.startsWith("$2b$"));
        
        // 验证密码
        boolean matches = encoder.matches(testPassword, bcryptEncoded);
        System.out.println("密码验证结果: " + matches);
    }
}