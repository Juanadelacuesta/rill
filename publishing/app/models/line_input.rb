class LineInput < Input

  has_many :answers

  def errors_when_publishing
    errors = []
    errors << "No correct answer for line input #{name} in #{inputable_type} '#{inputable.name}', in '#{inputable.parent}'" if answers.empty?
    errors << "Empty correct answer for line input #{name} in #{inputable_type} '#{inputable.name}', in '#{inputable.parent}'" unless answers.find_all{|answer| answer.value.blank?}.empty?
    errors
  end

  def to_publishing_format
    {
      name: "_INPUT_#{position}_",
      prefix: prefix,
      suffix: suffix,
      width: width,
      correct_answers: answers.map(&:value)
    }
  end

end