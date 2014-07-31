# Studyflow S3 deployment
set :deploy_to, "/home/studyflow/app"
set :s3path, "s3://studyflow-server-images"


desc 'Deploy application from S3'
task :deploy => ["deploy:deploy"]


namespace :deploy do

  task :deploy do
    throw "no valid GIT SHA given!" unless ENV['revision'] =~ /^[0-9a-f]{40}$/
    run_locally do
      info " -> deploying SHA: #{ ENV['revision'] }"
    end
    set :current_revision, ENV['revision']

    timestamp = Time.now.strftime("%Y%m%d%H%M%S")
    set(:release_timestamp, timestamp)
    set(:release_path, releases_path.join(timestamp))

    on release_roles(:all) do |host|
      execute :mkdir, '-pv', releases_path
      execute :mkdir, '-p', release_path
      within release_path do
        execute "s3cmd get  #{ fetch(:s3path) }/#{ fetch(:current_revision) }/*#{ host.roles.first }* #{ release_path }/"
        execute "cd #{ release_path } && ls -r | sed 1d | while read i ; do echo \" -> deleting older release $i\" ; rm \"\$i\"; done"
      end
    end

    on release_roles(:publish) do |host|
      within release_path do
        execute :tar, "-zxf", "*.tar"
        execute :rm, "-f", "*.tar"
      end
    end

    on release_roles(:all) do |host|
      within deploy_path do
        execute :rm, '-rf', current_path
        role = host.roles.first
        if role == :publish
          execute :ln, '-s', release_path, current_path
        else
          jar_file = capture "ls #{ release_path }/*.jar"
          execute :ln, '-s', release_path.join(jar_file), current_path
        end
      end
    end

    on release_roles(:all) do |host|
      role = host.roles.first
      if role == :publish
        execute :touch, current_path.join("tmp", "restart.txt")
      else
        execute :sudo, :supervisorctl, :restart, "studyflow_#{ role }"
      end
    end

  end


end # /namespace


def release_roles(*names)
  names << { exclude: :no_release } unless names.last.is_a? Hash
  roles(*names)
end
