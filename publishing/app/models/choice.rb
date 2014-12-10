class Choice < ActiveRecord::Base
  include HtmlParseable

  belongs_to :multiple_choice_input
  acts_as_list scope: :multiple_choice_input

  default_scope { order(:position) }

  scope :for_short_uuid, ->(id) { where(["SUBSTRING(CAST(id AS VARCHAR), 1, 8) = ?", id]) }
  def self.find_by_uuid(id, with_404 = true)
    choices = for_short_uuid(id)
    raise ActiveRecord::RecordNotFound if choices.empty? && with_404
    raise StudyflowPublishing::ShortUuidDoubleError.new("Multiple choices found for uuid: #{id}") if choices.length > 1
    choices.first
  end

  def to_publishing_format
    {
      value: preparse_text_for_publishing(value, "choice in #{reference}"),
      correct: correct
    }
  end

  def errors_when_publishing
    errors = []
    begin
      render_latex_for_publishing(value)
    rescue
      errors << "Errors in LaTeX rendering in #{reference}"
    end
    errors += parse_errors(:value, reference)
    errors += image_errors(:value, reference)
    errors
  end

  def to_param
    "#{id[0,8]}"
  end

  def reference
    "choice #{multiple_choice_input.position} in #{multiple_choice_input.inputable_type} #{multiple_choice_input.inputable.name}, in #{multiple_choice_input.inputable.quizzable}"
  end

end
