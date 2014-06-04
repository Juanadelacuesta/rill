class CoursesController < ApplicationController

  def select
    if params[:course] && params[:course][:id]
      @course = Course.find(params[:course][:id])
      Course.current = @course
      session[:course_id] = @course.id
    else
      Course.current = nil
      session[:course_id] = nil
    end
    redirect_to chapters_path
  end

end
