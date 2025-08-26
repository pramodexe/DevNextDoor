#!/bin/bash

# Fix XML files that start with comments instead of XML declarations
cd /home/user/Coding/DevNextDoor2/app/src/main/res/drawable

for file in *.xml; do
    if [ -f "$file" ]; then
        # Check if file starts with comment and doesn't have XML declaration
        if head -1 "$file" | grep -q "^<!--" && ! grep -q "<?xml" "$file"; then
            echo "Adding XML declaration to $file"
            # Create temp file with XML declaration + original content
            echo '<?xml version="1.0" encoding="utf-8"?>' > temp_file
            cat "$file" >> temp_file
            mv temp_file "$file"
        fi
    fi
done

echo "Finished processing XML files"
