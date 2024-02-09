require 'spec_helper'
require 'shared_spec'

describe 'Sending of emails fails' do
  it 'SMTP server not reachable' do
    email = FactoryBot.create(:email, :unsent)

    sleep(SEND_FREQUENCY_IN_SECONDS + 1)

    expect_until_timeout do
      email.reload
      expect(email.trials).to eq 1
      expect(email.is_successful).to be false
      expect(email.error_message).to match /Couldn't connect to host, port: localhost, \d+; timeout -1/
      expect(Email.count).to eq 1
      assert_not_received_email(email.from_address, email.to_address)
    end
  end
end
