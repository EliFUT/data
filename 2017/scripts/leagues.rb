require_relative 'common'

leagues = [1, 4, 10, 13, 14, 16, 17, 19, 20, 31, 32, 39, 41, 50, 53, 54, 56, 60, 61, 65, 66, 67, 68, 76, 78, 80, 83, 189, 308, 335, 336, 341, 350, 351, 353, 382, 383, 384, 2000, 2028]

leagues.each do |id|
  image_path = "../images/leagues/league_#{id}.png"
  unless File.exist?(image_path)
    `curl -s #{SRC_BASE_URL}/leagueLogos_sm/web/l#{id}.png > #{image_path}`
  end
end
