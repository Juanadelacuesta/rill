require 'rails_helper'

RSpec.describe ChoiceQuestion, :type => :model do

  it {is_expected.to validate_presence_of :text }
  it {is_expected.to have_many :choices}



end
