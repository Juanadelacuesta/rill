# Read about factories at https://github.com/thoughtbot/factory_girl

FactoryGirl.define do
  factory :extra_example do
    section { |a| a.association(:section) }
  end
end