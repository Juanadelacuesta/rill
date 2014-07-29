# Studyflow S3 deployment

Rake::Task["deploy:updating"].clear_actions

desc 'Compile and then upload application to S3'
task :build => ["deploy:upload"]


namespace :deploy do

  #############################################################################################
  # upload builded jar file

  desc 'Upload jar-file to S3'
  task upload: :new_release_path2 do
    run_locally do
      execute "s3cmd --multipart-chunk-size-mb=5 put target/*-SNAPSHOT-standalone.jar #{ fetch(:s3path) }/#{ fetch(:release_file) }"
    end
  end

  #############################################################################################
  # running

  desc 'Restart application'
  task :restart do
    on roles(:app) do
      execute "sudo supervisorctl restart #{ fetch(:supervisor_name) }"
    end
  end

  desc 'Update server(s) by setting up a new release.'
  task :updating => :new_release_path do
    invoke "deploy:set_current_revision2"
    invoke "deploy:create_release2"
  end

  desc "Place a REVISION file with the current revision SHA in the current release path"
  task :set_current_revision2 do
    on roles(:app) do
      within deploy_path do
        execute :echo, "\"#{fetch(:current_revision)}\" >> REVISION"
      end
    end
  end

  desc 'download application from S3'
  task create_release2: :update do
    on roles(:app) do
      last_commit = capture("cd #{ repo_path } && git rev-parse #{ fetch(:branch) }")
      ask :current_revision, last_commit || ""
      throw "no valid release SHA given! aborting..." unless fetch(:current_revision).length == 40
      set :release_file, "#{ fetch(:application) }-#{ fetch(:current_revision) }*.jar"
      execute :mkdir, '-p', release_path
      execute "s3cmd get #{ fetch(:s3path) }/#{ fetch(:release_file) } #{ release_path }/"
      execute "cd #{ release_path } && ls -r | sed 1d | while read i ; do echo \" -> deleting older release $i\" ; rm \"\$i\"; done"
      execute "ln -fs  #{ release_path }/#{ fetch(:release_file) } #{ release_path }/#{ fetch(:supervisor_name) }.jar"
    end
  end

  desc 'update repository'
  task update: :clone do
    on roles(:app) do
      execute "cd #{ repo_path } && git remote update"
    end
  end

  desc 'clone into repository'
  task :clone do
    on roles(:app) do
      if test("[ -d #{ repo_path} ]")
        info t(:mirror_exists, at: repo_path)
      else
        execute("git clone --mirror #{ fetch(:repo_url) } #{ repo_path }")
      end
    end
  end

  task :new_release_path2 do
    run_locally do
      set :current_revision, capture("git rev-parse HEAD")
      set :release_file, "#{ fetch(:application) }-#{ fetch(:current_revision) }-#{ Time.now.strftime("%Y%m%dT%H%M%S") }.jar"
    end
  end

end
