FactoryBot.modify do
  factory :user do
    email { Faker::Internet.email }
  end
end
