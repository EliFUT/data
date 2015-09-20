require_relative 'common'

total_nations = 219

1.upto(total_nations) do |id|
    `curl -s #{SRC_BASE_URL}/cardflagssmall/web/#{id}.png > #{normal_image_path}`
  end
end
