import pandas as pd
from os.path import dirname, join


def find_receipt(similar_recipes, number):
    for i in range(len(similar_recipes)):
        if similar_recipes[i][1] == number:
            return i


def recommend(similar_recipes, item_id, count=1):
    index = find_receipt(similar_recipes, item_id)

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
        return similar_recipes[index - count:index + count + 1 + 1]

def find_info(ds, url):
    return (ds.loc[[url],['num', 'score', 'Название', 'Изображение', 'likes', 'bookmarks']])


def test(count):
    return count + 2
filename1 = join(dirname(__file__), "data2.txt")
filename2 = join(dirname(__file__), "GfG.csv")
my_data = eval(open(filename1, 'r').read())
ds = pd.read_csv(filename2, index_col=0)
number_recomend = 55
def rec_system(number_recomend):
    recommend_receipt = recommend(my_data, number_recomend)
    for i in recommend_receipt:
        return(find_info(ds, i[0]))
rec_system(number_recomend)





