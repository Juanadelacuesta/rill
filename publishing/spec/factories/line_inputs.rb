# Read about factories at https://github.com/thoughtbot/factory_girl

FactoryGirl.define do
  factory :line_input do
    text "Default question text"
    question
  end
end
