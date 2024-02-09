MAIL_SERVER_SMTP_PORT = ENV.fetch('MADEK_MAIL_SMTP_PORT', 25)

def update_smtp_settings
  SmtpSetting.first.update(port: MAIL_SERVER_SMTP_PORT,
                           is_enabled: true)
end
