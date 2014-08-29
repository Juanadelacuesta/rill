##########################################################
# Studyflow S3 deployment
##########################################################
set :deploy_to, "/home/studyflow/app"
set :s3path, "s3://studyflow-server-images"
set :max_load_time, 120
set :keep_releases, 10
set :release_roles, [:login, :learning, :school, :publish]
set :rvm_roles, "publish"
set :log_level, :info

after 'deploy:finished', 'appsignal:deploy'