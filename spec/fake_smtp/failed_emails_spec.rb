require 'spec_helper'
require 'shared_spec'

describe 'Sending of emails fails' do
  it 'maximum trials reached' do
    email = FactoryBot.create(:email, :failed, trials: RETRIES_IN_SECONDS.length + 1)
    sleep(SEND_FREQUENCY_IN_SECONDS + RETRIES_IN_SECONDS.last + 1)

    expect_until_timeout do
      email_2 = email.dup
      email_2.id = email.id
      email_2.reload
      expect(email_2.trials).to eq email.trials
      expect(email_2.is_successful).to be false
      expect(email_2.error_message).to eq email.error_message
      expect(Email.count).to eq 1
      assert_not_received_email(email.from_address, email.to_address)
    end
  end

  it 'smtp server does not support TLS' do
    SmtpSetting.first.update(enable_starttls_auto: true)

    email = FactoryBot.create(:email, :unsent)

    sleep(SEND_FREQUENCY_IN_SECONDS + 1)

    expect_until_timeout do
      email.reload
      expect(email.trials).to be > 0
      expect(email.is_successful).to be false
      expect(email.error_message).to eq 'STARTTLS is required but host does not support STARTTLS'
      expect(Email.count).to eq 1
      assert_not_received_email(email.from_address, email.to_address)
    end
  end

  it 'fake sending if smtp disabled' do
    SmtpSetting.first.update(is_enabled: false)

    email = FactoryBot.create(:email, :unsent)
    sleep(SEND_FREQUENCY_IN_SECONDS + 1)

    expect_until_timeout do
      email.reload
      expect(email.trials).to eq 1
      expect(email.is_successful).to be false
      expect(email.error_message).to eq 'Not sent because of disabled SMTP setting.'
      expect(Email.count).to eq 1
      assert_not_received_email(email.from_address, email.to_address)
    end
  end
end
