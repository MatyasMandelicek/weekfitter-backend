@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = "http://localhost:3000/reset-password/" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Obnovení hesla - WeekFitter");
        message.setText("Klikněte na následující odkaz pro obnovení hesla:\n" + resetLink);
        mailSender.send(message);
    }
}
