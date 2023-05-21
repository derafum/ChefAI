from os.path import dirname, join

def find_index_receipt(similar_recipes, number):
    for i in range(len(similar_recipes)):
        if similar_recipes[i][1] == number:
            return i

def recommend(similar_recipes, item_id, count=2):
    index = find_index_receipt(similar_recipes, item_id)

    if index < count:
        n = count * 2 + 1
        #print(*similar_recipes[:n], sep="\n")
        return similar_recipes[:n]
    elif index > len(similar_recipes) - count - 1:
        n = count * 2
        #print(*similar_recipes[-1 * n:], sep="\n")
        return similar_recipes[-1 * n:]
    else:
        #print(*similar_recipes[index - count:index + count + 1 + 1], sep='\n')
        return similar_recipes[index - count:index + count + 1 ]


def rec_system(number_recomend, my_data=None):
    if my_data is None:
        filename = join(dirname(__file__), "data2.txt")
        my_data = eval(open(filename, 'r').read())
    recommend_receipt = recommend(my_data, number_recomend)
    return recommend_receipt




#number = 15
#print(rec_system(number))
