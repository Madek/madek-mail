require 'active_support/all'
ENV['RAILS_ENV'] = ENV['RAILS_ENV'].presence || 'test'

require 'pathname'
require 'faker'
require 'config/bundle'
require 'config/rails'
require 'config/database'
require 'config/emails'
require 'config/mail'
require 'pry'


RSpec.configure do |config|
  config.expect_with :rspec do |expectations|
    expectations.include_chain_clauses_in_custom_matcher_descriptions = true
  end

  config.mock_with :rspec do |mocks|
    mocks.verify_partial_doubles = true
  end

  config.shared_context_metadata_behavior = :apply_to_host_groups


  config.before(:example) do |example|
    srand 1
    update_smtp_settings
  end

end
