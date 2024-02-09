FactoryBot.define do
  factory :email do
    subject { Faker::Lorem.sentence }
    body { Faker::Lorem.paragraph }
    from_address { Faker::Internet.email }
    to_address { user.email }
    user
  end

  trait :unsent do
    trials { 0 }
  end

  trait :succeeded do
    trials { 1 }
    is_successful { true }
  end

  trait :failed do
    trials { 1 }
    is_successful { false }
    error_message { 'service unavailable' }
  end
end
